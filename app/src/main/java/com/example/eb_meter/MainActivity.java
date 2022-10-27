package com.example.eb_meter;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final int[] totalUnit = new int[7];
    private final float[] totalCharge = new float[7];

    Bitmap bmp, scaledbmp;

    // constant code for runtime permissions
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set name and account number on the screen
        TextView welcomeTxt = findViewById(R.id.welcomeTxt);
        TextView accountTxt = findViewById(R.id.accountView);

        //checks permission
        if (checkPermission()) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        //to get the name of the user from the previous activity
        welcomeTxt.setText(getString(R.string.welcomeMsg, getIntent().getStringExtra("name")));

        //to get the a/c number of the meter from the previous activity
        accountTxt.setText(getString(R.string.cebACNumber, getIntent().getStringExtra("accountNumber")));

        setUnits();
        setCharge();
        calcTotal();

        //refreshes randomly generated units and charges
        Button refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUnits();
                setCharge();
                calcTotal();
            }
        });

        //function call to generate PDF document
        Button createPdfBtn = findViewById(R.id.createPdfBtn);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pdf_header);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 1050, 369, false);
        createPdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPdfFunc();
            }
        });

    }

    //creates settings icon on title bar main menu
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //function to return a string if settings is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingMenu:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //function to generate PDF
    private void createPdfFunc() {
        // creating an object variable for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        // variable for paint
        Paint title = new Paint();

        // we are adding page info to our PDF file, in which we will be passing our pageWidth, pageHeight and number of pages
        // later, we are calling it to create our PDF.
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(792, 1120, 1).create();

        // below line is used for setting start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);

        // creating a variable for canvas from our page of PDF.
        Canvas canvas = myPage.getCanvas();

        // below line is used for adding typeface for our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        // below line is used for setting text size which we will be displaying in our PDF file.
        title.setTextSize(15);
        // below line is sued for setting color of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));

        // below line is used to draw text in our PDF file
        canvas.drawText("A smart device to be peaceful", 300, 320, title);
        canvas.drawText("Ceylon Electricity board", 300, 300, title);

        // after adding all attributes to our PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);

        // below line is used to set the name of our PDF file and its path.
        File file = new File(Environment.getExternalStorageDirectory(), "electricBill.pdf");

        try {
            // after creating a file name, we will write our PDF file to that location.
            pdfDocument.writeTo(new FileOutputStream(file));
            // prints toast message on completion of PDF generation.
            Toast.makeText(MainActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // error handling
            Log.e("main", "error " + e);
            e.printStackTrace();
            // toast message printing the error
            Toast.makeText(this, "Something wrong: " + e, Toast.LENGTH_SHORT).show();
        }
        // closes PDF file
        pdfDocument.close();
    }

    //function to generate random values
    private int generateRandomValues() {
        Random random = new Random();
        return random.nextInt(110 - 15) + 15;
    }

    //function to set units
    private void setUnits() {
        //initialize Unit textview for the rooms in the table
        TextView lrUnits = findViewById(R.id.lrUnits);
        TextView drUnits = findViewById(R.id.drUnits);
        TextView kitchenUnits = findViewById(R.id.kitchenUnits);
        TextView br1Units = findViewById(R.id.br1Units);
        TextView br2Units = findViewById(R.id.br2Units);
        TextView bt1Units = findViewById(R.id.bt1Units);
        TextView bt2Units = findViewById(R.id.bt2Units);

        //generate random units and store in TotalUnit ArrayList
        for(int i=0; i<7; ++i) {
            totalUnit[i] = generateRandomValues();
        }

        //set a random unit to the rooms in the table
        lrUnits.setText(String.valueOf(totalUnit[0]));
        drUnits.setText(String.valueOf(totalUnit[1]));
        kitchenUnits.setText(String.valueOf(totalUnit[2]));
        br1Units.setText(String.valueOf(totalUnit[3]));
        br2Units.setText(String.valueOf(totalUnit[4]));
        bt1Units.setText(String.valueOf(totalUnit[5]));
        bt2Units.setText(String.valueOf(totalUnit[6]));

    }

    //function to determine charges
    private float getCharge(int units) {
        float charge;

        if (units <= 90) {
            charge = (float) 16.00 * units;
        } else if (units <= 180) {
            charge = (float) 50.00 * units;
        } else {
            charge = (float) 75.00 * units;
        }

        return charge;
    }

    //function to set charges
    private void setCharge() {
        //initialize charges textview in the table
        TextView lrCharges = findViewById(R.id.lrCharges);
        TextView drCharges = findViewById(R.id.drCharges);
        TextView kitchenCharges = findViewById(R.id.kitchenCharges);
        TextView br1Charges = findViewById(R.id.br1Charges);
        TextView br2Charges = findViewById(R.id.br2Charges);
        TextView bt1Charges = findViewById(R.id.bt1Charges);
        TextView bt2Charges = findViewById(R.id.bt2Charges);

        //calculate charges to totalCharge ArrayList
        for (int i=0; i<7; i++) {
            totalCharge[i] = getCharge(totalUnit[i]);
        }

        //set calculated charges to their corresponding textview
        lrCharges.setText(getString(R.string.chargeFormat, totalCharge[0]));
        drCharges.setText(getString(R.string.chargeFormat, totalCharge[1]));
        kitchenCharges.setText(getString(R.string.chargeFormat, totalCharge[2]));
        br1Charges.setText(getString(R.string.chargeFormat, totalCharge[3]));
        br2Charges.setText(getString(R.string.chargeFormat, totalCharge[4]));
        bt1Charges.setText(getString(R.string.chargeFormat, totalCharge[5]));
        bt2Charges.setText(getString(R.string.chargeFormat, totalCharge[6]));
    }

    //function to calculate total charges and units
    private void calcTotal() {
        int totUnits = 0;
        float totCharges = (float) 0.00;

        //totalling units and charges for display
        for(int i=0; i<totalUnit.length; i++) {
            totUnits += totalUnit[i];
            totCharges += totalCharge[i];
        }

        //initializing textview of total row
        TextView totalUnits = findViewById(R.id.totalUnits);
        TextView totalCharges = findViewById(R.id.totalCharges);

        totalUnits.setText(String.valueOf(totUnits));
        totalCharges.setText(getString(R.string.chargeFormat, totCharges));

    }

    //function for checking permissions
    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED;
    }

    //function to request permission if it was not provided
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}