package lj.projetandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 *
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ColorPicker cp;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_ACCESS = 2;
    private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO_ACTIVITY_REQUEST_CODE = 1;
    private boolean canRead = false;
    private boolean canWrite = false;
    private boolean cameraAcces = false;
    private Bitmap originalOne = null;
    private Bitmap currentOne = null;
    private int currentRotation = 0;

    /*Modes liés à la seekbar
    0 -> Aucun
    1 -> Luminosité
    2 -> Contraste
     */
    private int seekBarMode = 0;

    /**
     * Check if the permissions required are granted or not
     * @param requestCode int
     * @param permissions String
     * @param grantResults int[]
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canRead = true;
                } else {
                    canRead = false;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canWrite = true;
                } else {
                    canWrite = false;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA_ACCESS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraAcces = true;
                } else {
                    cameraAcces = false;
                }
            }
        }
    }

    /**
     * Update the view
     */
    public void refreshView()
    {
        TouchImageView tiv = findViewById(R.id.tiv);
        tiv.setImageBitmap(currentOne);
    }

    /**
     * Saves the image currently shown in image view, if the permission write has been granted.
     * A new folder names "ModifiedImages" will be created if it does not exists.
     * The name of the image will be set as the current time in milliseconds.
     * When the image is saved, a temporary message is shown and says where the image has been saved
     */
    public void saveImg() {
        //If null, do nothing
        if(currentOne == null)
            return;

        if(!canWrite)
            return;

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/ModifiedImages");
        dir.mkdirs();
        @SuppressLint("DefaultLocale") String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        try {
            outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        currentOne.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outFile)));
        Toast.makeText(this, getResources().getString(R.string.save_toast) + sdCard.getAbsolutePath() +"/ModifiedImages/" + fileName, Toast.LENGTH_LONG).show();
        try {
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch a new intent if the permission Read has been granted.
     * This intent will open the gallery where the user can choose an image to open in the app.
     */
    public void selectPicture() {
        if(!canRead)
            return;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE_ACTIVITY_REQUEST_CODE);
    }

    private Uri uriFilePath;

    /**
     * Launch a new intent if the permission Camera Acces has been granted.
     * This intent will open the camera where the user can take a photo to use in the app.
     */
    public void takePhoto() {
        if(!cameraAcces)
            return;
        File mainDirectory = new File(Environment.getExternalStorageDirectory(), "/ModifiedImages");
        if (!mainDirectory.exists())
            mainDirectory.mkdirs();

        Calendar calendar = Calendar.getInstance();

        uriFilePath = FileProvider.getUriForFile(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                new File(mainDirectory, calendar.getTimeInMillis()+".jpg"));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriFilePath);
        startActivityForResult(intent, TAKE_PHOTO_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Handle the Intents created by takePhoto or selectPicture.
     * Save the path of the original image to permit the reset functionality
     * It will end by putting the image in the imageView.
     * @param requestCode int
     * @param resultCode int
     * @param imageReturnedIntent Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_PICTURE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);



                        originalOne = BitmapFactory.decodeFile(filePath);
                        int max = ( originalOne.getWidth() > originalOne.getHeight() ? originalOne.getWidth() : originalOne.getHeight());


                        if(max > 1980)
                        {
                            double fact = 1980.0 / max;
                            originalOne = Bitmap.createScaledBitmap(originalOne,(int) (originalOne.getWidth() * fact),(int) (originalOne.getHeight() * fact), true);
                        }

                        currentOne = originalOne.copy(Bitmap.Config.ARGB_8888, true);
                        currentRotation = 0;
                        refreshView();
                        TouchImageView tiv = findViewById(R.id.tiv);
                        tiv.setZoom(0.99f);
                    }
                    cursor.close();
                }
                break;
            case TAKE_PHOTO_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        originalOne = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriFilePath);
                        int max = ( originalOne.getWidth() > originalOne.getHeight() ? originalOne.getWidth() : originalOne.getHeight());


                        if(max > 1980)
                        {
                            double fact = 1980.0 / max;
                            originalOne = Bitmap.createScaledBitmap(originalOne,(int) (originalOne.getWidth() * fact),(int) (originalOne.getHeight() * fact), true);
                        }
                        currentOne = originalOne.copy(Bitmap.Config.ARGB_8888, true);
                        currentRotation = 0;
                        refreshView();
                        TouchImageView tiv = findViewById(R.id.tiv);
                        tiv.setZoom(0.99f);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    /**
     * Apply changes when the seekbar of the contrast or luminosity option is used
     * @param v View
     */
    public void validateSeekbar(View v){
        SeekBar sk = ( findViewById(R.id.seekbar));
        if(seekBarMode == 1)
        {

            int value = sk.getProgress() - sk.getMax() / 2;
            currentOne = BitmapModifier.changeLuminosity(currentOne, value);
        }
        else
        {
            double value = 2.0 * sk.getProgress() / sk.getMax();
            currentOne = BitmapModifier.changeContrast(currentOne, value);

        }
        (findViewById(R.id.layout_seekbar)).setVisibility(View.INVISIBLE);
        refreshView();
        sk.setProgress(sk.getMax() / 2);
    }

    /**
     * Cancels all the modifications made on the image
     */
    public void reinit(){
        if(originalOne != null) {
            currentOne = originalOne.copy(Bitmap.Config.ARGB_8888, true);
            refreshView();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //Cela signifie que la permission à déjà été demandée et l'utilisateur l'a refusé
                //On peut aussi expliquer à l'utilisateur pourquoi cette permission est nécessaire et la redemander
            } else {
                //Sinon demander la permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        }
        else
        {
            canRead = true;
        }
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        }
        else
        {
            canWrite = true;
        }
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA_ACCESS);
            }
        }
        else
        {
            cameraAcces = true;
        }

        cp = new ColorPicker(this);
        /*SeekBar seekBar = (SeekBar)findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                if(seekBarMode == 1)
                {

                    int value = seekBar.getProgress() - seekBar.getMax() / 2;
                    currentOne = BitmapModifier.changeLuminosity(currentOne, value);
                }
                else
                {
                    double value = 2.0 * seekBar.getProgress() / seekBar.getMax();
                    currentOne = BitmapModifier.changeContrast(currentOne, value);

                }
                refreshView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });*/
    }

    /**
     * Permit the user to press back on the drawer to retract it
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflate the menu ; this adds items to the action bar if it is present.
     * @param menu Menu
     * @return always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * Launch the method when the corresponding button in the menu is pressed
     * @param item MenuItem
     * @return true if there is no image loaded in the app
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        seekBarMode = 0;

        if(id == R.id.galerie)
        {
            selectPicture();
        }
        else if(id == R.id.photo)
        {
            takePhoto();
        }

        //If null, do nothing
        if(originalOne == null && !(id==R.id.galerie||id==R.id.photo)) {
            Toast.makeText(this, R.string.warning, Toast.LENGTH_LONG).show();
            return true;
        }

        // Handle navigation view item clicks here.
        if(id == R.id.save)
        {
            saveImg();
        }
        else if(id == R.id.reinit)
        {
            reinit();
        }
        else if (id == R.id.lumino) {
            seekBarMode = 1;
        } else if (id == R.id.contra) {
            seekBarMode = 2;
        } else if (id == R.id.gris) {
            currentOne = BitmapModifier.changeTint(currentOne, 0, null);
            refreshView();
        }
        else if(id == R.id.sepia){
            currentOne = BitmapModifier.changeTint(currentOne, 1, null);
            refreshView();
        }
        else if(id == R.id.tint){
            /* Show color picker dialog */
            cp.show();
            cp.setCallback(new ColorPickerCallback() {
                @Override
                public void onColorChosen(@ColorInt int color) {

                    currentOne = BitmapModifier.changeTint(currentOne,0, color);
                    refreshView();
                    cp.dismiss();
                }
            });
            //refreshView();
        }
        else if(id == R.id.histo)
        {
            currentOne = BitmapModifier.equalizeHisto(currentOne);
            refreshView();
        }
        else if(id == R.id.Gaussien3 )
        {
            final float[][] gauss = {{1.f/16, 1.f/8, 1.f/16},
                    {1.f/8, 1.f/4, 1.f/8},
                    {1.f/16, 1.f/8, 1.f/16}};
            currentOne = BitmapModifier.convolution(currentOne,gauss,3);
            refreshView();
        }
        else if(id == R.id.Gaussien5 )
        {
            final float[][] gauss = {{1.f/256, 4.f/256, 6.f/256, 4.f/256, 1.f/256},
                    {4.f/256, 16.f/256, 24.f/256, 16.f/256, 4.f/256},
                    {6.f/256, 24.f/256, 36.f/256, 24.f/256, 6.f/256},
                    {4.f/256, 16.f/256, 24.f/256, 16.f/256, 4.f/256},
                    {1.f/256, 4.f/256, 6.f/256, 4.f/256, 1.f/256}};
            currentOne = BitmapModifier.convolution(currentOne,gauss,5);
            refreshView();
        }
        else if(id == R.id.Gaussien7 )
        {
            final float[][] gauss = {{0.000036f,	0.000363f,	0.001446f,	0.002291f,	0.001446f,	0.000363f,	0.000036f},
                    {0.000363f,	0.003676f,	0.014662f,	0.023226f,	0.014662f,	0.003676f,	0.000363f},
                    {0.001446f,	0.014662f,	0.058488f,	0.092651f,	0.058488f,	0.014662f,	0.001446f},
                    {0.002291f,	0.023226f,	0.092651f,	0.146768f,	0.092651f,	0.023226f,	0.002291f},
                    {0.001446f,	0.014662f,	0.058488f,	0.092651f,	0.058488f,	0.014662f,	0.001446f},
                    {0.000363f,	0.003676f,	0.014662f,	0.023226f,	0.014662f,	0.003676f,	0.000363f},
                    {0.000036f,	0.000363f,	0.001446f,	0.002291f,	0.001446f,	0.000363f,	0.000036f}};
            currentOne = BitmapModifier.convolution(currentOne,gauss,7);
            refreshView();
        }
        else if(id == R.id.moyen )
        {
            final float[][] moyen = {{1.f/9, 1.f/9, 1.f/9},
                    {1.f/9, 1.f/9, 1.f/9},
                    {1.f/9, 1.f/9, 1.f/9}};
            currentOne = BitmapModifier.convolution(currentOne,moyen,3);
            refreshView();
        }
        else if(id == R.id.Laplacien )
        {
            final float[][] laplacian = {{0, 1, 0},
                    {1, -4, 1},
                    {0, 1, 0}};
            currentOne = BitmapModifier.convolution(currentOne,laplacian,3);
            refreshView();
        }
        else if(id == R.id.Sobel )
        {
            currentOne = BitmapModifier.sobelEdgeDetection(currentOne);
            refreshView();
        }
        else if(id == R.id.rotateRight)
        {
            currentOne = BitmapModifier.rotateBitmap(currentOne, -90);
            currentRotation -= 90;
            refreshView();
        }
        else if(id == R.id.rotateLeft)
        {
            currentOne = BitmapModifier.rotateBitmap(currentOne, 90);
            currentRotation += 90;
            refreshView();
        }
        else if(id == R.id.cartoon)
        {
            currentOne = BitmapModifier.cartoonFilter(currentOne);
            refreshView();
        }

        if(seekBarMode != 0)
        {
            (findViewById(R.id.layout_seekbar)).setVisibility(View.VISIBLE);
        }
        else
            (findViewById(R.id.layout_seekbar)).setVisibility(View.INVISIBLE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
