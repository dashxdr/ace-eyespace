package ace.eyespace;

import java.nio.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import java.util.Random;
import android.opengl.GLUtils;
import android.util.FloatMath;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.FloatMath;
import android.view.MotionEvent;

class eye {
	public float x, y, r, color;
};


public class mrenderer implements GLSurfaceView.Renderer {
	final private int MAXCOORDS = 256;
	private FloatBuffer coordVB, textureVB;
	private int mProgram;
	private int maPositionHandle;
	private int matPosHandle;
	private int time = 0;
	private static final String TAG = "MyLog";
	private Context ourcontext;
	private int mMVPMatrixHandle;
	private float[] mProjectionMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mModelMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
	private float sw, sh;
	private float sw2, sh2;
	private int dwidth, dheight;
	private mview ourview;
	private int[] texture = new int[1];
	private int circle_CENTER, circle_IRADIUS, circle_DIRECTION;
	private int circle_LIGHT, circle_COLOR;
	private float lightx, lighty, lightz;
	private float xpos = 0.0f, ypos = 0.0f;
	private double scale = 1.0f;
	private float rawx, rawy;
	final private int NUMEYES = 300;
	final private int BMSIZE = 256;
	private eye[] eyes = new eye[NUMEYES];
	private int numeyes;
	private Random generator;
	private long now;

	public  mrenderer(Context context, mview aview)
	{
		ourcontext = context;
		ourview = aview;
	}

	private int loadShader(int type, String shaderCode){

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
Log.e(TAG, GLES20.glGetShaderInfoLog(shader));

		return shader;
	}

	private int load_res_Shader(int type, int fID){

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);
		StringBuffer fs = new StringBuffer();

		try {
			// Now read FS
			InputStream inputStream = ourcontext.getResources().openRawResource(fID);
			// setup Bufferedreader
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

			String read = in.readLine();
			while (read != null) {
				fs.append(read + "\n");
				read = in.readLine();
			}
			fs.deleteCharAt(fs.length() - 1);
		} catch (Exception e) {

		}


		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, fs.toString());
		GLES20.glCompileShader(shader);

Log.e(TAG, GLES20.glGetShaderInfoLog(shader));

		return shader;
	}



	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Set the background frame color
Log.d(TAG, "GL_EXTENSIONS = " + GLES20.glGetString(GLES20.GL_EXTENSIONS));
Log.d(TAG, "GL_SHADING_LANGUAGE_VERSION = " + GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION));

		int[] testval = new int[1];
		testval[0] = 123456;
		GLES20.glGetIntegerv(GLES20.GL_NUM_SHADER_BINARY_FORMATS, testval, 0);
		Log.d(TAG, "GL_NUM_SHADER_BINARY_FORMATS = " + testval[0]);
		GLES20.glGetIntegerv(GLES20.GL_SHADER_BINARY_FORMATS, testval, 0);
		Log.d(TAG, "GL_SHADER_BINARY_FORMATS[0] = " + testval[0]);


		GLES20.glClearColor(0.0f, 0.0f, 0.2f, 1.0f);
		// initialize the triangle vertex array

		ByteBuffer vbb, vbb2;
		vbb = ByteBuffer.allocateDirect(MAXCOORDS * 4);
		vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
		coordVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
		vbb2 = ByteBuffer.allocateDirect(MAXCOORDS * 4);
		vbb2.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
		textureVB = vbb2.asFloatBuffer();

		int vertexShader = load_res_Shader(GLES20.GL_VERTEX_SHADER,
					R.raw.circle_vs);
		int fragmentShader = load_res_Shader(GLES20.GL_FRAGMENT_SHADER,
					R.raw.circle_fs);

		mProgram = GLES20.glCreateProgram();			 // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(mProgram);				  // creates OpenGL program executables

		// get handle to the vertex shader's vPosition member
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		matPosHandle = GLES20.glGetAttribLocation(mProgram, "tPosition");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
		circle_CENTER = GLES20.glGetUniformLocation(mProgram, "CENTER");
		circle_IRADIUS = GLES20.glGetUniformLocation(mProgram, "IRADIUS");
		circle_DIRECTION = GLES20.glGetUniformLocation(mProgram, "DIRECTION");
		circle_LIGHT = GLES20.glGetUniformLocation(mProgram, "LIGHT");
		circle_COLOR = GLES20.glGetUniformLocation(mProgram, "COLOR");

		Bitmap bm = Bitmap.createBitmap(BMSIZE, BMSIZE, Bitmap.Config.ARGB_8888);
		generator = new Random();
		for(int j=0;j<BMSIZE;++j)
		{
			int color = generator.nextInt();

			for(int i=0;i<BMSIZE;++i)
			{
				int outc = 0;
				if(i>=BMSIZE*95/100)
					outc = 0;
				else if(i>=BMSIZE*80/100)
					outc = color;
				else
					outc = 0xffffff;
				bm.setPixel(i, j, 0xff000000 | outc);
			}
		}
		GLES20.glGenTextures(1, texture, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

        // parameters
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
			GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
			GLES20.GL_TEXTURE_MAG_FILTER,
			GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
			GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
			GLES20.GL_CLAMP_TO_EDGE);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bm, 0);

		lightx = -1.0f;
		lighty = 1.0f;
		lightz = 0.8f;
		float lf = 1.0f / FloatMath.sqrt(lightx*lightx + lighty*lighty + lightz*lightz);
		lightx *= lf;
		lighty *= lf;
		lightz *= lf;

	}

	public void onDrawFrame(GL10 unused) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);// | GLES20.GL_DEPTH_BUFFER_BIT);
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glUniform3f(circle_LIGHT, lightx, lighty, lightz);

		float eyedx, eyedy, eyedz;
		eyedx = rawx;
		eyedy = rawy;
		eyedz = 100.0f;
		float ef = 1.0f / FloatMath.sqrt(eyedx*eyedx + eyedy*eyedy + eyedz*eyedz);
		eyedx *= ef;
		eyedy *= ef;
		eyedz *= ef;

		GLES20.glUniform3f(circle_DIRECTION, eyedx, eyedy, eyedz);

//		DrawEye(rawx, rawy, 100.0f / (float)scale, 0.0f);
//
//		for(int i=0;i<8;++i)
//		{
//			float x = 50.0f * (i%4 - 1.5f);
//			float y = 50.0f * (i/4 - .5f);
//			DrawEye(x, y, 24.0f, (i+0.5f)/256.0f);
//		}

		long last = now;
		now = System.currentTimeMillis();
		last = now - last;
		if(last>1000) last = 1000;
		while(last>0)
		{
			advanceEyes();
			last -= 20;
		}
		for(int i=0;i<numeyes;++i)
			DrawEye(eyes[i]);

	}

	void initEye(int n, float range)
	{
		eye e = new eye();

		for(;;)
		{
			int i;
			e.x = (generator.nextFloat() - 0.5f) * sw2 * range;
			e.y = (generator.nextFloat() - 0.5f) * sh2 * range;
			e.r = FloatMath.sqrt(e.x*e.x + e.y*e.y) - 2.0f;
			if(e.r < 0.0f) continue;
			e.r = Math.min(e.r, sw - e.x);
			e.r = Math.min(e.r, sw + e.x);
			e.r = Math.min(e.r, sh - e.y);
			e.r = Math.min(e.r, sh + e.y);
			for(i=0;i<numeyes;++i)
			{
				if(i==n) continue;
				eye e2 = eyes[i];
				float dx, dy, r2;
				dx = e2.x - e.x;
				dy = e2.y - e.y;
				r2 = FloatMath.sqrt(dx*dx + dy*dy);
				if(r2 < e2.r) break; // inside this circle
				e.r = Math.min(e.r, r2 - e2.r);
			}
			if(i==numeyes)
				break;
		}
		e.color = ((generator.nextInt() & (BMSIZE-1)) + 0.5f) / (float)BMSIZE;
		eyes[n] = e;
	}

	void advanceEyes()
	{
		for(int i=0;i<numeyes;++i)
		{
			final float ZOOM = 1.01f;
			eye e = eyes[i];
			e.x *= ZOOM;
			e.y *= ZOOM;
			e.r *= ZOOM;
			if(e.x + e.r > -sw &&
					e.x - e.r < sw &&
					e.y + e.r > -sh &&
					e.y - e.r < sh &&
					e.r < 1000.0f)
				continue;
			initEye(i, .2f);
		}
	}

	void DrawEye(eye e)
	{
		int i, j;
		float a, re;

		final int numvert = 8;
		float [] Coords = new float[3*(numvert+2)];

		j = 0;
		Coords[j+0] = e.x;
		Coords[j+1] = e.y;
		Coords[j+2] = 0.0f;
		j+=3;

		re = e.r / FloatMath.cos(3.14159f / numvert);
		for(i=0;i<=numvert;++i)
		{
			a = i*3.14159f * 2.0f / numvert;
			Coords[j+0] = e.x + re * FloatMath.cos(a);
			Coords[j+1] = e.y + re * FloatMath.sin(a);
			Coords[j+2] = 0.0f;
			j+=3;
		}
		coordVB.put(Coords); // add the coordinates to the FloatBuffer
		coordVB.position(0); // set the buffer to read the first coordinate
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, coordVB);


		float eyedx, eyedy, eyedz;
		eyedx = -e.x;
		eyedy = -e.y;
		eyedz = 80.0f;
		float ef = 1.0f / FloatMath.sqrt(eyedx*eyedx + eyedy*eyedy + eyedz*eyedz);
		eyedx *= ef;
		eyedy *= ef;
		eyedz *= ef;

		GLES20.glUniform3f(circle_DIRECTION, eyedx, eyedy, eyedz);

		GLES20.glUniform3f(circle_CENTER, e.x, e.y, 0.0f);
		GLES20.glUniform1f(circle_IRADIUS, 1.0f / e.r);
		GLES20.glUniform1f(circle_COLOR, e.color);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, numvert+2);

	}


	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
//		final float fix = .00040f * ((width>height) ? width : height);
		final float pushback = 20.0f;
		float fix = pushback * ((width<height) ? width : height);
		fix = 100.0f / fix;
		final float right = fix * width;
		final float left = -right;
		final float top = fix * height;
		final float bottom = -top;
		sw = right * pushback;
		sw2 = sw*2.0f;
		sh = top * pushback;
		sh2 = sh*2.0f;
		dwidth = width;
		dheight = height;
		final float near = 1.0f;
		final float far = 1000.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, mModelMatrix, 0, 0.0f, 0.0f, -pushback);



		if(false) // eye crap
		{
			// Position the eye behind the origin.
			final float eyeX = 0.0f;
			final float eyeY = 0.0f;
			final float eyeZ = 0.0f; //1.5f;

			// We are looking toward the distance
			final float lookX = 0.0f;
			final float lookY = 0.0f;
			final float lookZ = -5.0f;

			// Set our up vector. This is where our head would be pointing were we holding the camera.
			final float upX = 0.0f;
			final float upY = 1.0f;
			final float upZ = 0.0f;

			// Set the view matrix. This matrix can be said to represent the camera position.
			// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
			// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
			Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
			Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		} else
		{
// with this setup a central square on the display, as large as it can be
// without going offscreen, has corners at (+/- 100, +/- 100, 0)
// we can adjust "pushback" for how much perspective effect we want.
// Larger means less perspective.
			mMVPMatrix = mModelMatrix;
		}

		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		numeyes = 0;
		for(numeyes=0;numeyes < NUMEYES;++numeyes)
		{
			initEye(numeyes, 1.0f);
		}
		now = System.currentTimeMillis();
	}

	private int buttonstate = 0;
	private float downx0, downy0;
	private float currx0, curry0;
	private float downx1, downy1;
	private float currx1, curry1;
	private double cdist;

	private double dist(float x1, float y1, float x2, float y2)
	{
		x1 -= x2;
		y1 -= y2;
		return Math.sqrt(x1*x1 + y1*y1);
	}

	public void processevent(android.view.MotionEvent e)
	{
		int c = e.getPointerCount();
		int action = e.getAction();
		int oldb = buttonstate;
		int pid = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
				MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		action &= ~MotionEvent.ACTION_POINTER_INDEX_MASK;
//Log.d(TAG, "                           action = " + action + ", pid = " + pid);

		rawx = (e.getX() / dwidth - 0.5f) * sw2;
		rawy = (0.5f - e.getY() / dheight) * sh2;

		if(action==MotionEvent.ACTION_POINTER_DOWN)
			action = MotionEvent.ACTION_DOWN;
		if(action==MotionEvent.ACTION_UP)
		{
			if(c==1)
				pid = e.getPointerId(0);
		}
		if(action==MotionEvent.ACTION_POINTER_UP)
			action = MotionEvent.ACTION_UP;

		if(action==MotionEvent.ACTION_UP)
			buttonstate &= ~(1<<pid);

		for(int i = 0;i<c;++i)
		{
			int id = e.getPointerId(i);
			if(id>1) continue;
			float x, y;
			x = e.getX(i);
			y = e.getY(i);
			if(id==0)
			{
				if(action==MotionEvent.ACTION_DOWN)
				{
					buttonstate|=1;
					currx0 = downx0 = x;
					curry0 = downy0 = y;
				}
				if((buttonstate&1) != 0)
				{
					xpos += (x - currx0) * 0.25f;// / dwidth;
					ypos -= (y - curry0) * 0.25f;// / dheight;
					currx0 = x;
					curry0 = y;
				}
			} else if(id==1)
			{
				if(action==MotionEvent.ACTION_DOWN)
				{
					buttonstate|=2;
					currx1 = downx1 = x;
					curry1 = downy1 = y;
				}
				if((buttonstate&2) != 0)
				{
					currx1 = x;
					curry1 = y;
				}
			}
		}
		if((oldb&3)!=3 && (buttonstate&3)==3)
		{
			cdist = dist(currx0, curry0, currx1, curry1);
		}
//if(oldb != buttonstate) Log.d(TAG, "New button state = " + buttonstate);
		if((buttonstate&3) == 3)
		{
			double dnow = dist(currx0, curry0, currx1, curry1);
			double newscale = scale * cdist / dnow;
			cdist = dnow;
//Log.d(TAG, "Before: xpos = " + xpos);

			double tx = (currx0 / dwidth - .5f);
			double ty = (curry0 / dheight - .5f);
//			xpos = (float)(tx + (xpos - tx) * scale / newscale);
//			ypos = (float)(ty + (ypos - ty) * scale / newscale);
			scale = newscale;
//Log.d(TAG, "After: xpos = " + xpos);


		}
	}
}
