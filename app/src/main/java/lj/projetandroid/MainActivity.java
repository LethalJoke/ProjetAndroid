package lj.projetandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
    private boolean canRead = false;

    private Bitmap originalOne;

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
        }
    }

    public void selectPicture(View v) {
        if(!canRead)
            return;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE_ACTIVITY_REQUEST_CODE);
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
        }
    }

    public void validateSeekbar(View v){
        ImageView iv = ((ImageView)findViewById(R.id.imageView2));
        Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        SeekBar sk = ((SeekBar) findViewById(R.id.seekbar));
        if(seekBarMode == 1)
        {

            int value = sk.getProgress() - sk.getMax() / 2;
            iv.setImageBitmap(BitmapModifier.changeLuminosity(bmp, value));
        }
        else
        {
            double value = 2.0 * sk.getProgress() / sk.getMax();
            iv.setImageBitmap(BitmapModifier.changeContraste(bmp, value));

        }
        ((LinearLayout)findViewById(R.id.layout_seekbar)).setVisibility(View.INVISIBLE);
        sk.setProgress(sk.getMax() / 2);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        seekBarMode = 0;

        if (id == R.id.lumino) {
            seekBarMode = 1;
        } else if (id == R.id.contra) {
            seekBarMode = 2;
        } else if (id == R.id.gris) {
            ImageView iv = ((ImageView)findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            iv.setImageBitmap(BitmapModifier.changeTeinte(bmp, 0));
        }
        else if(id == R.id.sepia){
            ImageView iv = ((ImageView)findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            iv.setImageBitmap(BitmapModifier.changeTeinte(bmp, 1));
        }
        else if(id == R.id.histo)
        {
            ImageView iv = ((ImageView)findViewById(R.id.imageView2));
            Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            iv.setImageBitmap(BitmapModifier.egalisationHistogramme(bmp));
        }

        if(seekBarMode != 0)
        {
            ((LinearLayout)findViewById(R.id.layout_seekbar)).setVisibility(View.VISIBLE);
        }
        else
            ((LinearLayout)findViewById(R.id.layout_seekbar)).setVisibility(View.INVISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
