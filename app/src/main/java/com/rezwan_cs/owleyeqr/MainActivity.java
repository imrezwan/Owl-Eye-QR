package com.rezwan_cs.owleyeqr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.rezwan_cs.owleyeqr.owleyeqr.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar ;
    FirebaseDatabase firebaseDatabase ;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        progressBar = findViewById(R.id.loading_spinner);
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("qrcodes");

    }

    private Bitmap printQRCode(String textToQR){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(textToQR, BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*public void onClick(View view) {
       // EditText text = findViewById(R.id.editText);
        String text = UUID.randomUUID().toString();
        if (text.toString().isEmpty()){
            Toast.makeText(this, "Kindly enter any text first", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap QRBit = printQRCode(text.toString());
        if (QRBit == null){
            Toast.makeText(this, "Unable to generate code!", Toast.LENGTH_SHORT).show();
        }else {
            Intent qRIntent = new Intent(this, ShowPrintQR.class);
            qRIntent.putExtra("bitmap", QRBit);
            qRIntent.putExtra("string", text);
            startActivity(qRIntent);
        }


    }*/


    public void onClick(View view){
        generateQRCodes();
    }

    private void generateQRCodes() {
        if(isStoragePermissionGranted()){
            progressBar.setVisibility(View.VISIBLE);
            EditText editText = findViewById(R.id.editText);
            String ss = editText.getText().toString().trim();
            int number = Integer.valueOf(ss);
            while(number>0){
                String text = UUID.randomUUID().toString();
                Bitmap bitmap = printQRCode(text);
                doOneQRPrint(bitmap, text);
                storeOneQRCodeToFirebase(new QRCodeClass(text,
                        false));
                number--;
            }
            progressBar.setVisibility(View.GONE);
            editText.setText("");
            Toast.makeText(this,
                    ss+" QR codes have generated on \"OWL EYE QR\" folder",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void storeOneQRCodeToFirebase(QRCodeClass qrCodeClass) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", qrCodeClass.code);
        values.put("activation", qrCodeClass.activation);
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(qrCodeClass.code).setValue(values);
        //Map<String, String> map = new HashMap<>();
        //map.put("timestamp", ServerValue.TIMESTAMP.get("timestamp"))
        //Log.d("TAG", values.entrySet().toString());
    }

    private void doOneQRPrint(Bitmap qRBit, String text) {
        //String text = getIntent().getStringExtra("string");
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(qRBit.getWidth(),
                qRBit.getHeight(),1).create();
        PdfDocument.Page  page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawPaint(paint);

        qRBit = Bitmap.createScaledBitmap(qRBit,
                qRBit.getWidth(), qRBit.getHeight(), true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(qRBit, 0, 0, null);
        pdfDocument.finishPage(page);

        //save to
        File root = new File(
                Environment.getExternalStorageDirectory(),
                "OWL EYE QR");

        if(!root.exists()){
            root.mkdir();
        }

        File file = new File(root, text+".pdf");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            pdfDocument.writeTo(fileOutputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
              //  Log.v(TAG,"Permission is granted");
                return true;
            } else {

              //  Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
           // Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            generateQRCodes();
        }
    }
}
