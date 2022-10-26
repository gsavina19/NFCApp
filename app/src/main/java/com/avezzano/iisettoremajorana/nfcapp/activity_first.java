package com.avezzano.iisettoremajorana.nfcapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

public class activity_first extends AppCompatActivity {


    NfcAdapter nfcAdapter;
    ToggleButton tg1ReadWrite;
    //da 4 a 7
    EditText txtTagContent, txtPadre,txtTagContent7,txtTagContent8,txtTagContent11,txtTagContent12;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        //et_message= (EditText)findViewById(R.id.et_message);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        tg1ReadWrite = (ToggleButton) findViewById(R.id.tg1ReadWrite);
        txtTagContent = (EditText) findViewById(R.id.txtTagContent);
        txtPadre = (EditText) findViewById(R.id.txtPadre);
        txtTagContent7 = (EditText) findViewById(R.id.txtTagContent7);
        txtTagContent8 = (EditText) findViewById(R.id.txtTagContent8);
      //  txtTagContent11 = (EditText) findViewById(R.id.txtTagContent11);
        txtTagContent12 = (EditText) findViewById(R.id.txtTagContent12);
        if (nfcAdapter == null) {
            // NFC is not available for device
            Toast.makeText(this, "Nfc non disponibile :(", Toast.LENGTH_LONG).show();
        } else
            if (!nfcAdapter.isEnabled()) {
            // NFC is available for device but not enabled
            //PopUp
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("NFC Spento")
                    .setMessage("Perfavore attivate l'NFC per poter utilizzare l'app")
                    .setPositiveButton("Impostazioni", null)
                    .show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();//aperte le impostazioni chiude il messaggio
                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                }
            });
        } else {
            // NFC is enabled
            Toast.makeText(this, "Nfc attivo", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();

        enableForegroundDispatchSystem();


    }

    @Override
    protected void onPause() {
        super.onPause();

        disableForegroundDispatchSystem();

    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if (intent.hasExtra(nfcAdapter.EXTRA_TAG)){
            Toast.makeText(this, "Tag scritto", Toast.LENGTH_LONG).show();

            if(tg1ReadWrite.isChecked()){

                Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if(parcelables != null && parcelables.length > 0){

                    readTextFromMessage((NdefMessage)parcelables[0]);

                }else {

                    Toast.makeText(this, "NO NDEF record found! ", Toast.LENGTH_LONG).show();

                }

            }else {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                NdefMessage ndefMessage = createNdefMessage("Nome: "+txtTagContent.getText()+";"+"\n"+"Cognome: "+txtPadre.getText()+";"+"\n"+"Telefono mamma: "+txtTagContent7.getText()+";"+"\n"+"Telefono papa: "+txtTagContent8.getText()+";"+"\n"+"Patologie:       "+txtTagContent12.getText()+"");

                writeNdefMessage(tag, ndefMessage);


            }

        }

    }

    private void readTextFromMessage(NdefMessage ndefMessage){

         NdefRecord[] ndefRecords = ndefMessage.getRecords();

         if(ndefRecords != null && ndefRecords.length>0){

             NdefRecord ndefRecord = ndefRecords[0];

             String tagContent = getTextFromNdefRecord(ndefRecord);

             txtTagContent.setText(tagContent);


         }else {
             Toast.makeText(this, "NO NDEF records found! ", Toast.LENGTH_LONG).show();
         }

    }

    private void enableForegroundDispatchSystem(){

        Intent intent = new Intent(this, activity_first.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[]intentFilter = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
    }

    private void disableForegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);

    }

    private void formatTag(Tag tag, NdefMessage ndefMessage){

        try {

            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if(ndefFormatable == null){
                Toast.makeText(this, "Nfc tag is not ndef formable", Toast.LENGTH_SHORT).show();
            }

            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();

            Toast.makeText(this, "Tag NFC Scritto", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Log.e("formatTag", e.getMessage());
        }
    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage){

        try {

            if(tag == null){
                Toast.makeText(this, "TagOBJ cam't be null", Toast.LENGTH_SHORT).show();
                return;
            }

            Ndef ndef = Ndef.get(tag);

            if (ndef == null){

                //format tag with ndef format + writes the message
                formatTag(tag, ndefMessage);
            }else{
                ndef.connect();

                if(!ndef.isWritable()){
                    Toast.makeText(this, "Il Tag NFC Non è scrivibile", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "Tag NFC Scritto", Toast.LENGTH_SHORT).show();

            }

        }catch (Exception e){
            Log.e("writeNdefmessage", e.getMessage());
        }
    }

    private NdefRecord createTextRecord (String message) {
        try {
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = message.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;

            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) (languageSize & 0x1F));
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
        }catch (UnsupportedEncodingException e) {
            Log.e("createTextRecord", e.getMessage());
        }
        return null;
    }

    private NdefMessage createNdefMessage(String content){


        NdefRecord ndefRecord = createTextRecord(content);

        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ ndefRecord });

        return ndefMessage;
    }

    public void tg1ReadWriteOnClick(View  view){
        txtTagContent.setText("");
        txtPadre.setText("");
        txtTagContent7.setText("");
        txtTagContent8.setText("");
        txtTagContent11.setText("");
        txtTagContent12.setText("");
    }

    public String getTextFromNdefRecord(NdefRecord ndefRecord){
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize +1,
                    payload.length - languageSize - 1, textEncoding);
        }catch (UnsupportedEncodingException e) {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

}

