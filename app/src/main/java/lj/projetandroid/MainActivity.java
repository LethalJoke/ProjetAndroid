package lj.projetandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

   private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
   private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
   private static final int MY_PERMISSIONS_REQUEST_CAMERA_ACCESS = 2;
   private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;
   private static final int TAKE_PHOTO_ACTIVITY_REQUEST_CODE = 1;
   private boolean canRead = false;
   private boolean canWrite = false;
   private boolean cameraAcces = false;
   private Bitmap originalOne = null;

   /*Modes liés à la seekbar
   0 -> Aucun
   1 -> Luminosité
   2 -> Contraste
    */
    private int seekBarMode = 0;

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

        public void saveImg() {
            //If null, do nothing
            if(originalOne == null)
                return;

            if(!canWrite)
                return;

            ImageView iv = (findViewById(R.id.imageView2));
            BitmapDrawable draw = (BitmapDrawable) iv.getDrawable();
            Bitmap bitmap = draw.getBitmap();

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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
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

    public void selectPicture() {
        if(!canRead)
            return;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE_ACTIVITY_REQUEST_CODE);
    }

    private Uri uriFilePath;

    public void takePhoto() {
        if(!cameraAcces)
            return;
        File mainDirectory = new File(Environment.getExternalStorageDirectory(), "/Camera");
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
                        ((ImageView)findViewById(R.id.imageView2)).setImageBitmap(originalOne);
                    }
                    cursor.close();
                }
                break;
            case TAKE_PHOTO_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        originalOne = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriFilePath);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    ((ImageView)findViewById(R.id.imageView2)).setImageBitmap(originalOne);
                }
            break;
        }
    }


    public void validateSeekbar(View v){
        ImageView iv = (findViewById(R.id.imageView2));
        Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        SeekBar sk = ( findViewById(R.id.seekbar));
        if(seekBarMode == 1)
        {

            int value = sk.getProgress() - sk.getMax() / 2;
            iv.setImageBitmap(BitmapModifier.changeLuminosity(bmp, value));
        }
        else
        {
            double value = 2.0 * sk.getProgress() / sk.getMax();
            iv.setImageBitmap(BitmapModifier.changeContrast(bmp, value));

        }
        (findViewById(R.id.layout_seekbar)).setVisibility(View.INVISIBLE);
        sk.setProgress(sk.getMax() / 2);
    }

    public void reinit(){
        if(originalOne != null)
            ((ImageView)findViewById(R.id.imageView2)).setImageBitmap(originalOne);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        seekBarMode = 0;

        if(id == R.id.galerie)
        {
            selectPicture();
        }
        else if(id == R.id.reinit)
        {
            reinit();
        }

        //If null, do nothing
        if(originalOne == null)
            return true;

        // Handle navigation view item clicks here.
        if(id == R.id.save)
        {
            saveImg();
        }
        else if(id == R.id.photo)
        {
            takePhoto();
        }
        else if (id == R.id.lumino) {
            seekBarMode = 1;
        } else if (id == R.id.contra) {
            seekBarMode = 2;
        } else if (id == R.id.gris) {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            iv.setImageBitmap(BitmapModifier.changeTint(bmp, 0));
        }
        else if(id == R.id.sepia){
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            iv.setImageBitmap(BitmapModifier.changeTint(bmp, 1));
        }
        else if(id == R.id.histo)
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            iv.setImageBitmap(BitmapModifier.equalizeHisto(bmp));
        }
        else if(id == R.id.Gaussien3 )
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            final float[][] gauss = {{1.f/16, 1.f/8, 1.f/16},
                    {1.f/8, 1.f/4, 1.f/8},
                    {1.f/16, 1.f/8, 1.f/16}};
            iv.setImageBitmap(BitmapModifier.convolution(bmp,gauss,3));
        }
        else if(id == R.id.Gaussien5 )
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            final float[][] gauss = {{1.f/256, 4.f/256, 6.f/256, 4.f/256, 1.f/256},
                    {4.f/256, 16.f/256, 24.f/256, 16.f/256, 4.f/256},
                    {6.f/256, 24.f/256, 36.f/256, 24.f/256, 6.f/256},
                    {4.f/256, 16.f/256, 24.f/256, 16.f/256, 4.f/256},
                    {1.f/256, 4.f/256, 6.f/256, 4.f/256, 1.f/256}};
            iv.setImageBitmap(BitmapModifier.convolution(bmp,gauss,5));
        }
        else if(id == R.id.Gaussien7 )
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            final float[][] gauss = {{0.000036f,	0.000363f,	0.001446f,	0.002291f,	0.001446f,	0.000363f,	0.000036f},
                    {0.000363f,	0.003676f,	0.014662f,	0.023226f,	0.014662f,	0.003676f,	0.000363f},
                    {0.001446f,	0.014662f,	0.058488f,	0.092651f,	0.058488f,	0.014662f,	0.001446f},
                    {0.002291f,	0.023226f,	0.092651f,	0.146768f,	0.092651f,	0.023226f,	0.002291f},
                    {0.001446f,	0.014662f,	0.058488f,	0.092651f,	0.058488f,	0.014662f,	0.001446f},
                    {0.000363f,	0.003676f,	0.014662f,	0.023226f,	0.014662f,	0.003676f,	0.000363f},
                    {0.000036f,	0.000363f,	0.001446f,	0.002291f,	0.001446f,	0.000363f,	0.000036f}};
            iv.setImageBitmap(BitmapModifier.convolution(bmp,gauss,7));
        }
        else if(id == R.id.moyen )
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            final float[][] moyen = {{1.f/9, 1.f/9, 1.f/9},
                    {1.f/9, 1.f/9, 1.f/9},
                    {1.f/9, 1.f/9, 1.f/9}};
            iv.setImageBitmap(BitmapModifier.convolution(bmp,moyen,3));
        }
        else if(id == R.id.Laplacien )
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            final float[][] laplacian = {{0, 1, 0},
                    {1, -4, 1},
                    {0, 1, 0}};
            iv.setImageBitmap(BitmapModifier.convolution(bmp,laplacian,3));
        }
        else if(id == R.id.Sobel )
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            final float[][] laplacian = {{-1, -2, -1},
                    {0, 0, 0},
                    {1, 2, 1}};
            iv.setImageBitmap(BitmapModifier.convolution(bmp,laplacian,3));
        }
        else if(id == R.id.rotate)
        {
            ImageView iv = (findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            iv.setImageBitmap(BitmapModifier.rotateBitmap(bmp,90));
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
