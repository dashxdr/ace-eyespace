package ace.eyespace;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class mainActivity extends Activity
{
	private static final String TAG = "MyLog";
	private GLSurfaceView mGLView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
Log.d(TAG, "Here in mainActivity");
//		setContentView(R.layout.main);

		requestWindowFeature(Window.FEATURE_NO_TITLE);					
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity.
		mGLView = new mview(this);
		setContentView(mGLView);
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
Log.d(TAG, "Here in onDestroy");
	}

	@Override
	public void onSaveInstanceState(Bundle bundle)
	{
		super.onSaveInstanceState(bundle);
		bundle.putInt("test.val", 23);
	}
	@Override
	protected void onRestoreInstanceState(Bundle bundle)
	{
		super.onRestoreInstanceState(bundle);
Log.d(TAG, "RestoreInstance - test.val = " + bundle.getInt("test.val", -1));
	}

	@Override
	protected void onPause() {
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		mGLView.onResume();
	}
}

//********************************************
//********************************************
//********************************************
class mview extends GLSurfaceView
{
	private static final String TAG = "MyLog";
	private mrenderer renderer;

	public mview(Context context){
		super(context);
		  
		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);
		// Set the Renderer for drawing on the GLSurfaceView
		renderer = new mrenderer(context, this);
		setRenderer(renderer);
		setRenderMode(RENDERMODE_WHEN_DIRTY);

	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		renderer.processevent(e);
		requestRender();
		return true;
	}
}

