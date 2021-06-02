package com.hooliganlabs.infinityftf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.math.MathUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import com.hooliganlabs.infinityftf.databinding.ActivityMainBinding;
import com.hooliganlabs.infinityftf.databinding.ViewModCardBinding;
import com.hooliganlabs.infinityftf.databinding.ViewSuccessTableBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int[] CM_LENGTHS = {20, 40, 60, 80, 100, 120, 240};

    private Context context;

    private ActivityMainBinding binding;
    private List<WeaponDataModel> weapons;
    private WeaponDataModel yourWeapon;
    private WeaponDataModel enemyWeapon;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        item.setChecked(!item.isChecked());

        if(item.isChecked()) {
            item.setTitle("Inches");

            binding.YourSuccessTable.AmericanMeasurements.setVisibility(View.GONE);
            binding.EnemySuccessTable.AmericanMeasurements.setVisibility(View.GONE);

            binding.YourSuccessTable.MetricMeasurements.setVisibility(View.VISIBLE);
            binding.EnemySuccessTable.MetricMeasurements.setVisibility(View.VISIBLE);
        } else {
            item.setTitle("Centimeters");

            binding.YourSuccessTable.AmericanMeasurements.setVisibility(View.VISIBLE);
            binding.EnemySuccessTable.AmericanMeasurements.setVisibility(View.VISIBLE);

            binding.YourSuccessTable.MetricMeasurements.setVisibility(View.GONE);
            binding.EnemySuccessTable.MetricMeasurements.setVisibility(View.GONE);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.context = this;

        String metaJsonStr = inputStreamToString(getResources().openRawResource(R.raw.metadata));
        MetaDataModel metaData = new Gson().fromJson(metaJsonStr, MetaDataModel.class);

        // If we find the Combi Rifle in the list, we will jump to that instead
        int defaultPos = 0;

        List<String> weaponNames = new ArrayList<>();
        weapons = new ArrayList<>();
        for(WeaponDataModel weapon : metaData.weapons) {
            if(weapon.distance != null && !weapons.contains(weapon)) {
                weaponNames.add(weapon.name);
                weapons.add(weapon);

                if (weapon.name.equals("Combi Rifle")) {
                    defaultPos = weapons.size()-1;
                }
            }
        }

        // Your Card
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, weaponNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.YourModCard.WeaponSpinner.setAdapter(spinnerArrayAdapter);

        binding.YourModCard.BallisticSkillSliderValue.setText(String.valueOf((int)binding.YourModCard.BallisticSkillSlider.getValue()));
        binding.YourModCard.BallisticSkillSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.YourModCard.BallisticSkillSliderValue.setText(String.valueOf((int)value));
                calculateSuccessValues(yourWeapon, binding.YourModCard, binding.YourSuccessTable);
            }
        });

        binding.YourModCard.SuppressiveFireChip.setOnClickListener(yourModsChipListener);
        binding.YourModCard.MartialArtsChip.setOnClickListener(yourModsChipListener);
        binding.YourModCard.SurpriseChip.setOnClickListener(yourModsChipListener);
        binding.YourModCard.CoverChip.setOnClickListener(yourModsChipListener);
        binding.YourModCard.FireTeamChip.setOnClickListener(yourModsChipListener);
        binding.YourModCard.XVisorChip.setOnClickListener(yourModsChipListener);
        binding.YourModCard.MimetismChipGroup.setOnCheckedChangeListener(yourModsChangedListener);
        binding.YourModCard.VisibilityChipGroup.setOnCheckedChangeListener(yourModsChangedListener);

        binding.YourModCard.WeaponSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yourWeapon = weapons.get(position);
                calculateSuccessValues(yourWeapon, binding.YourModCard, binding.YourSuccessTable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.YourModCard.WeaponSpinner.setSelection(defaultPos);

        // Enemy Card

        binding.FaceToFaceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                binding.DisabledOverlay.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                binding.EnemySuccessLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        ArrayAdapter<String> enemySpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, weaponNames);
        enemySpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.EnemyModCard.WeaponSpinner.setAdapter(enemySpinnerArrayAdapter);

        binding.EnemyModCard.BallisticSkillSliderValue.setText(String.valueOf((int)binding.YourModCard.BallisticSkillSlider.getValue()));
        binding.EnemyModCard.BallisticSkillSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.EnemyModCard.BallisticSkillSliderValue.setText(String.valueOf((int)value));
                calculateSuccessValues(enemyWeapon, binding.EnemyModCard, binding.EnemySuccessTable);
            }
        });

        binding.EnemyModCard.SkillTitle.setText("Enemy Skill");

        binding.EnemyModCard.ModTitle.setText("+ MODs for Enemy");
        binding.EnemyModCard.ModTitleOpponent.setText("- MODs for Enemy");

        binding.EnemyModCard.SuppressiveFireChip.setOnClickListener(enemyModsChipListener);
        binding.EnemyModCard.MartialArtsChip.setOnClickListener(enemyModsChipListener);
        binding.EnemyModCard.SurpriseChip.setOnClickListener(enemyModsChipListener);
        binding.EnemyModCard.CoverChip.setOnClickListener(enemyModsChipListener);
        binding.EnemyModCard.FireTeamChip.setOnClickListener(enemyModsChipListener);
        binding.EnemyModCard.XVisorChip.setOnClickListener(enemyModsChipListener);
        binding.EnemyModCard.MimetismChipGroup.setOnCheckedChangeListener(enemyModsChangedListener);
        binding.EnemyModCard.VisibilityChipGroup.setOnCheckedChangeListener(enemyModsChangedListener);

        binding.EnemyModCard.WeaponSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                enemyWeapon = weapons.get(position);
                calculateSuccessValues(enemyWeapon, binding.EnemyModCard, binding.EnemySuccessTable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.EnemyModCard.WeaponSpinner.setSelection(defaultPos);

        new AlertDialog.Builder(context)
        .setTitle("Always Respect your Opponent!")
        .setMessage("Use this app to be Quick and Accurate with the other player.")
                .setNeutralButton("Let's roll", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private View.OnClickListener yourModsChipListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            calculateSuccessValues(yourWeapon, binding.YourModCard, binding.YourSuccessTable);
        }
    };

    private ChipGroup.OnCheckedChangeListener yourModsChangedListener = new ChipGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(ChipGroup group, int checkedId) {
            calculateSuccessValues(yourWeapon, binding.YourModCard, binding.YourSuccessTable);
        }
    };

    private View.OnClickListener enemyModsChipListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            calculateSuccessValues(enemyWeapon, binding.EnemyModCard, binding.EnemySuccessTable);
        }
    };

    private ChipGroup.OnCheckedChangeListener enemyModsChangedListener = new ChipGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(ChipGroup group, int checkedId) {
            calculateSuccessValues(enemyWeapon, binding.EnemyModCard, binding.EnemySuccessTable);
        }
    };

    private void calculateSuccessValues(WeaponDataModel weapon, ViewModCardBinding modCard, ViewSuccessTableBinding successTable) {
        if(weapon == null)
            return;

        int mimMod = 0;
        int visMod = 0;
        int cov = 0;
        int fts = 0;
        int sa = 0;
        int ma = 0;
        int sf = 0;
        boolean xVisor = false;

        int bs = (int)modCard.BallisticSkillSlider.getValue();

        int selectedMimChipId = modCard.MimetismChipGroup.getCheckedChipId();
        if(selectedMimChipId != -1)
            mimMod = (selectedMimChipId == R.id.Mimetism_Chip_Neg3 ? -3 : -6);

        int selectedVisibilityChipId = modCard.VisibilityChipGroup.getCheckedChipId();
        if(selectedVisibilityChipId != -1)
            visMod = (selectedVisibilityChipId == R.id.Visibility_Chip_Low ? -3 : -6);

        List<Integer> selectedMyMODsChipIds = modCard.MyMODsChipGroup.getCheckedChipIds();
        if(selectedMyMODsChipIds.contains(R.id.FireTeam_Chip))
            fts = 3;
        if(selectedMyMODsChipIds.contains(R.id.XVisor_Chip))
            xVisor = true;

        List<Integer> selectedMiscChipIds = modCard.MiscChipGroup.getCheckedChipIds();
        if(selectedMiscChipIds.contains(R.id.Cover_Chip))
            cov = -3;
        if(selectedMiscChipIds.contains(R.id.Surprise_Chip))
            sa = -3;
        if(selectedMiscChipIds.contains(R.id.MartialArts_Chip))
            ma = -3;
        if(selectedMiscChipIds.contains(R.id.SuppressiveFire_Chip))
            sf = -3;

        for(int i = 0; i < CM_LENGTHS.length; i++) {
            Integer weapMod = null;
            int length = CM_LENGTHS[i];

            // Does the weapon have a range band?
            if(weapon.distance != null) { // Yes, has range bands
                if (weapon.distance.length1 != null && length <= weapon.distance.length1.length) {
                    weapMod = weapon.distance.length1.mod;
                } else if (weapon.distance.length2 != null && length <= weapon.distance.length2.length) {
                    weapMod = weapon.distance.length2.mod;
                } else if (weapon.distance.length3 != null && length <=  weapon.distance.length3.length) {
                    weapMod = weapon.distance.length3.mod;
                } else if (weapon.distance.length4 != null && length <= weapon.distance.length4.length) {
                    weapMod = weapon.distance.length4.mod;
                }

                if(weapMod == null) { // Not relevant anyway, do not show
                    ((TextView) successTable.SuccessValueRow.getChildAt(i)).setText("-");
                } else {
                    if(xVisor && weapMod < 0) // Adjust negative range bands for XVisor
                        weapMod += 3;

                    // When calculating final success value, are the mods within the +/-12 limit?
                    int modTotal = MathUtils.clamp(weapMod + cov + visMod + mimMod + fts + ma + sa + sf, -12, 12);

                    int successValue = bs + modTotal; // Now add skill
                    ((TextView) successTable.SuccessValueRow.getChildAt(i)).setText(successValue > 0 ? String.valueOf(successValue) : "-");
                }
            } else { // No, not relevant for this app
                ((TextView)successTable.SuccessValueRow.getChildAt(i)).setText("-");
            }
        }
    }

    public String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            String json = new String(bytes);
            return json;
        } catch (IOException e) {
            return null;
        }
    }
}