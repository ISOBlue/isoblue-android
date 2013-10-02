package org.isoblue.ISOBlueDemo;

import java.util.HashSet;
import java.util.Set;

import org.isoblue.isobus.PGN;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;

public class PGNDialogFragment extends DialogFragment {

	private TypedArray mPGNVals;
	private final Set<PGN> mSelectedPGNs = new HashSet<PGN>();
	
	public Set<PGN> getPGNs() {
		return mSelectedPGNs;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		
		mPGNVals.recycle();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mPGNVals = getResources().obtainTypedArray(R.array.pgn_vals);
		
		boolean checked[] = new boolean[mPGNVals.length()];
		for(int i = 0; i < mPGNVals.length(); i++) {
			checked[i] = mSelectedPGNs.contains(new PGN(mPGNVals.getInteger(i, -1)));
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Set the dialog title
		builder.setTitle(R.string.select_pgns)
		// Specify the list array, the items to be selected by default
		// (null for none),
		// and the listener through which to receive callbacks when
		// items are selected
				.setMultiChoiceItems(R.array.pgn_names, checked,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								PGN val = new PGN(mPGNVals.getInteger(which, -1));
								if (isChecked) {
									// If the user checked the item, add it to
									// the selected items
									mSelectedPGNs.add(val);
								} else if (mSelectedPGNs.contains(val)) {
									// Else, if the item is already in the
									// array, remove it
									mSelectedPGNs.remove(val);
								}
							}
						});

		return builder.create();
	}
}
