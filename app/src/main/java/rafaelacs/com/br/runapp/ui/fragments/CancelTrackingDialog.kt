package rafaelacs.com.br.runapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rafaelacs.com.br.runapp.R

/*
 * CancelTrackingDialog
 *
 * Create a dialog to cancel the run if it started already.
 *
 */
class CancelTrackingDialog : DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Cancelar a Corrida?")
                .setMessage("Tem certeza que quer cancelar a corrida e apagar todos os dados?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Sim") { _, _ ->
                    yesListener?.let { yes ->
                        yes()
                    }
                }
                .setNegativeButton("Não") { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .create()

    }

}