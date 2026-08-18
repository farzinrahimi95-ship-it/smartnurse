package com.smartnurse.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class MedicationAdapter extends ArrayAdapter<Medication> {

    private Context context;
    private List<Medication> medications;
    private MainActivity mainActivity;

    public MedicationAdapter(Context context, List<Medication> medications) {
        super(context, R.layout.item_medication, medications);
        this.context = context;
        this.medications = medications;
        this.mainActivity = (MainActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_medication, parent, false);
        }

        Medication med = medications.get(position);

        TextView tvName = convertView.findViewById(R.id.tvMedName);
        TextView tvDetails = convertView.findViewById(R.id.tvMedDetails);
        Button btnEdit = convertView.findViewById(R.id.btnEdit);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        tvName.setText(med.getName() + " - " + med.getTime());
        tvDetails.setText(med.getInstructions() + "\nتلفن: " + med.getPhone());

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.editMedication(med);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete(med);
            }
        });

        return convertView;
    }

    private void confirmDelete(final Medication med) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("حذف دارو");
        builder.setMessage("آیا از حذف این دارو مطمئن هستید؟");
        builder.setPositiveButton("بله", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                dbHelper.deleteMedication(med.getId());
                mainActivity.cancelAlarm(med.getId());
                mainActivity.loadMedicationList();
                Toast.makeText(context, "حذف شد", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("خیر", null);
        builder.show();
    }
}
