package rafaelacs.com.br.runapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import rafaelacs.com.br.runapp.R
import rafaelacs.com.br.runapp.other.Constants.KEY_NAME
import rafaelacs.com.br.runapp.other.Constants.KEY_WEIGHT
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /*
     * Create the view so the user can enter his/her name and weight.
     *
     * @params View view
     * @params Bundle Saved instance state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()
        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success) {
                Snackbar.make(view, "Mudanças salvas", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Por favor, preencha todos os campos", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    /*
     * Load fields inserted by the user.
     */
    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    /*
     * Apply changes to the fields inserted by the user.
     *
     * @return true
     */
    private fun applyChangesToSharedPref(): Boolean {
        val nameText = etName.text.toString()
        val weightText = etWeight.text.toString()
        if(nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()
        val toolbarText = "Run, $nameText!"
        requireActivity().tvToolbarTitle.text = toolbarText

        return true
    }

}