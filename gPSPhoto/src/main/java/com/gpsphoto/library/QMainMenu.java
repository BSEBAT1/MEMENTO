package com.gpsphoto.library;

import com.example.gpsphoto.R;

import android.content.Context;

public class QMainMenu extends QButtonDialog {
	public QMainMenu(Context mct) {
		super(mct);
		// TODO Auto-generated constructor stub
		this.addMenu("Gallery", R.drawable.gallery);
		// this.addMenu("",0);
		// this.addMenu(mct.getString(R.string.lbSearch),
		// R.drawable.menu_search);
		this.addMenu("Take a Photo", R.drawable.camera);

	}
}
