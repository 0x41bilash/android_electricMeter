package com.example.eb_meter;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final int[] totalUnit = new int[7];
    private final float[] totalCharge = new float[7];

    //height and width for PDF file
    int pageHeight = 1120;
    int pageWidth = 792;

    // Stores header for PDF
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

        //get image header into a Bitmap variable
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pdf_header);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

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
        //object variable for PDF document
        Toast.makeText(this, "Generating PDF", Toast.LENGTH_SHORT).show();
        PdfDocument pdfDocument = new PdfDocument();

        Paint paint = new Paint();
        Paint title = new Paint();

        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);

        Canvas canvas = myPage.getCanvas();
        canvas.drawBitmap(scaledbmp, 56, 40, paint);

        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        title.setTextSize(15);
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));

        canvas.drawText("Smart IoT systems empowering you.", 209, 100, title);
        canvas.drawText("ABC electricity usage meter", 209, 80, title);

        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));
        title.setTextSize(15);

        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("This is sample document which we have created", 396, 560, title);

        pdfDocument.finishPage(myPage);

        //write the above to PDF file and save in phone memory
        File file = new File(Environment.getExternalStorageDirectory(), "GeneratedBill.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(MainActivity.this, "PDF file generated successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    //function to request permission if it was not provided
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}