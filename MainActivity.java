package com.example.sklepikjc4p;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerComputer;
    CheckBox checkMouse;
    CheckBox checkKeyboard;
    CheckBox checkWebcam;
    EditText editName;
    TextView textTotalPrice;
    Button buttonSubmit, buttonSendEmail;
    ListView listViewOrders;

    DatabaseHelper dbHelper;
    int totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerComputer = findViewById(R.id.spinnerComputer);
        checkMouse = findViewById(R.id.checkMouse);
        checkKeyboard = findViewById(R.id.checkKeyboard);
        checkWebcam = findViewById(R.id.checkWebcam);
        editName = findViewById(R.id.editName);
        textTotalPrice = findViewById(R.id.textTotalPrice);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSendEmail = findViewById(R.id.buttonSendEmail);
        listViewOrders = new ListView(this);

        dbHelper = new DatabaseHelper(this);

        setupSpinner();
        setListeners();
    }

    private void setupSpinner() {
        String[] computers = {"Komputer Intel i5, 16GB RAM", "Komputer AMD Ryzen 7, 32GB RAM", "Komputer Intel i9, 64GB RAM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, computers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerComputer.setAdapter(adapter);
    }

    private void setListeners() {
        checkMouse.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotalPrice());
        checkKeyboard.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotalPrice());
        checkWebcam.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotalPrice());

        spinnerComputer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                calculateTotalPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        buttonSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Imię i nazwisko nie może być puste", Toast.LENGTH_SHORT).show();
                return;
            }

            submitOrder();
            String orderDetails = generateOrderDetails();

        });

        buttonSendEmail.setOnClickListener(v -> {
            String name = editName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Imię i nazwisko nie może być puste", Toast.LENGTH_SHORT).show();
                return;
            }

            String orderDetails = generateOrderDetails();
            sendEmail(name, orderDetails, totalPrice);
        });
    }

    private void calculateTotalPrice() {
        totalPrice = 0;

        switch (spinnerComputer.getSelectedItemPosition()) {
            case 0:
                totalPrice += 2000;
                break;
            case 1:
                totalPrice += 2500;
                break;
            case 2:
                totalPrice += 3000;
                break;
        }

        if (checkMouse.isChecked()) totalPrice += 35;
        if (checkKeyboard.isChecked()) totalPrice += 19;
        if (checkWebcam.isChecked()) totalPrice += 89;

        textTotalPrice.setText("Cena: " + totalPrice + " zł");
    }

    private String generateOrderDetails() {
        StringBuilder orderDetails = new StringBuilder();

        String computer = spinnerComputer.getSelectedItem().toString();
        orderDetails.append("Komputer: ").append(computer).append("\n");

        if (checkMouse.isChecked()) orderDetails.append("Dodatek: Myszka Dell MS116 (35 zł)\n");
        if (checkKeyboard.isChecked()) orderDetails.append("Dodatek: Klawiatura TITANUM TK101 (19 zł)\n");
        if (checkWebcam.isChecked()) orderDetails.append("Dodatek: Kamera DUXO WEBCAM-X13 (89 zł)\n");

        return orderDetails.toString();
    }

    private void submitOrder() {
        String name = editName.getText().toString();
        String orderDetails = generateOrderDetails();

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", name);
        values.put("PRICE", totalPrice);
        values.put("DATE", date);

        long newRowId = db.insert("orders", null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "Zamówienie zapisane!", Toast.LENGTH_SHORT).show();
            textTotalPrice.setText("Cena: 0 zł");
            clearSelections();
        } else {
            Toast.makeText(this, "Błąd zapisu zamówienia", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String name, String orderDetails, int totalPrice) {
        String recipient = "example@example.com";
        String subject = "Twoje zamówienie";
        String message = "Dziękujemy za złożenie zamówienia!\n" +
                "Imię i nazwisko: " + name + "\n" +
                orderDetails + "\n" +
                "Łączna cena: " + totalPrice + " zł";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(intent, "Wyślij e-mail..."));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Brak aplikacji do wysyłania e-maili", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSelections() {
        checkMouse.setChecked(false);
        checkKeyboard.setChecked(false);
        checkWebcam.setChecked(false);
        editName.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_orders) {
            showOrders();
            return true;
        } else if (id == R.id.menu_info) {
            Toast.makeText(this, "Autor: Jan Czarnowski", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.menu_sms) {
            sendSMS();
            return true;
        } else if (id == R.id.menu_save_cart) {
            saveCartSettings();
            return true;
        } else if (id == R.id.menu_share_cart) {
            shareCartContents();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void saveCartSettings() {
        String settings = "Komputer: " + spinnerComputer.getSelectedItem().toString() + "\n";

        if (checkMouse.isChecked()) settings += "Dodatek: Myszka\n";
        if (checkKeyboard.isChecked()) settings += "Dodatek: Klawiatura\n";
        if (checkWebcam.isChecked()) settings += "Dodatek: Kamera\n";

        Toast.makeText(this, "Ustawienia koszyka zapisane:\n" + settings, Toast.LENGTH_LONG).show();
    }

    private void shareCartContents() {
        StringBuilder cartContents = new StringBuilder("Zawartość koszyka:\n");
        cartContents.append("Komputer: ").append(spinnerComputer.getSelectedItem().toString()).append("\n");

        if (checkMouse.isChecked()) cartContents.append("Dodatek: Myszka\n");
        if (checkKeyboard.isChecked()) cartContents.append("Dodatek: Klawiatura\n");
        if (checkWebcam.isChecked()) cartContents.append("Dodatek: Kamera\n");

        cartContents.append("Łączna cena: ").append(totalPrice).append(" zł");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, cartContents.toString());
        startActivity(Intent.createChooser(shareIntent, "Udostępnij zawartość koszyka..."));
    }


    private void showOrders() {
        ArrayList<String> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM orders", null);


        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
            int price = cursor.getInt(cursor.getColumnIndexOrThrow("PRICE"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("DATE"));

            String order = "Zamawiający: " + name +
                    "\nData: " + date +
                    "\nCena: " + price + " zł";
            orders.add(order);
        }
        cursor.close();

        if (orders.isEmpty()) {
            Toast.makeText(this, "Brak zamówień do wyświetlenia", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lista zamówień");

        ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orders);
        listView.setAdapter(adapter);

        builder.setView(listView);
        builder.setPositiveButton("Zamknij", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void sendSMS() {
        String name = editName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(this, "Imię i nazwisko nie może być puste", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderDetails = generateOrderDetails();
        String smsBody = "Twoje zamówienie zostało złożone.\n" +
                "Imię i nazwisko: " + name + "\n" +
                orderDetails + "\n" +
                "Łączna cena: " + totalPrice + " zł";

        Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
        smsIntent.putExtra("sms_body", smsBody);

        try {
            startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Błąd podczas wysyłania SMS", Toast.LENGTH_SHORT).show();
        }
    }

}
