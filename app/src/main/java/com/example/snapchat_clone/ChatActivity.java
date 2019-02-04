package com.example.snapchat_clone;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ChatActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		ViewPager viewPager = findViewById(R.id.chatViewPager);
		viewPager.setAdapter(new pageAdapter(getSupportFragmentManager()));
	}

	private class pageAdapter extends FragmentPagerAdapter {

		public pageAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
				case 0: return new BitmojiChatFragment();
				default: return new BitmojiChatFragment();
			}
		}

		@Override
		public int getCount() {
			return 1;
		}
	}

	/*
  Enables fullscreen mode in the app
  - hides the notification bar and navigation bar
  - will show again if you swipe down from the top
  */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			);
		}
	}
}
