package com.sebekerga.findwhitesquare;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private CameraBridgeViewBase mOpenCvCameraView;
	private Mat mIntermediateMat;
	private SeekBar Piner;
	private Button Snaper;
	int granica = 150, MidX = 0, MidY = 0, counter = 0, Array[][] = new int[2][2], ArrayCounter = 0;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
		mOpenCvCameraView.setMaxFrameSize(640, 384);
		
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		Snaper = (Button) findViewById(R.id.Snap);
;		Snaper.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ToScreen(Integer.toString(MidX*6/383)+";"+Integer.toString(MidY*6/383));
				switch(ArrayCounter){
					case 0:Array[0][0] = MidX*6/383; Array[0][1] = MidX*6/383; ArrayCounter++; break;
					case 1:Array[1][0] = MidX*6/383; Array[1][1] = MidX*6/383; ArrayCounter++; 
					if(Array[0][0]>Array[1][0])ToScreen("Первый левее второго на "+Integer.toString(Array[0][0]-Array[1][0]));
					if(Array[0][0]==Array[1][0])ToScreen("Первый и второй одинаково расположены по горизонтали");
					if(Array[0][0]<Array[1][0])ToScreen("Первая правее второго на "+Integer.toString(Array[1][0]-Array[0][0]));
					if(Array[0][1]>Array[1][1])ToScreen("Первый ниже второго на "+Integer.toString(Array[0][1]-Array[1][1]));
					if(Array[0][1]==Array[1][1])ToScreen("Первый и второй одинаково расположены по вертикали");
					if(Array[0][1]<Array[1][1])ToScreen("Первая выше второго на "+Integer.toString(Array[1][1]-Array[0][1]));
						
					ArrayCounter = 0;
				}
			}
		});
		
		Piner = (SeekBar) findViewById(R.id.Pin);
		Piner.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				granica = Piner.getProgress();				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				granica = Piner.getProgress();	
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				granica = Piner.getProgress();	
			}
		});
    }

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat rgba = inputFrame.rgba();
		Size sizeRgba = rgba.size();
		Mat rgbaInnerWindow;

		int MidXX = 0, MidYY = 0;

		int rows = (int) sizeRgba.height;
		int cols = (int) sizeRgba.width;

		Mat grey = inputFrame.gray();

		Mat greyInnerWindow = grey.submat(0, 383, 127, 512);
		rgbaInnerWindow = rgba.submat(0, 383, 127, 512);

		Imgproc.cvtColor(rgbaInnerWindow, greyInnerWindow, Imgproc.COLOR_RGBA2GRAY);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(400);

		// blur
		Imgproc.GaussianBlur(greyInnerWindow, greyInnerWindow, new Size(9, 9), 10);
		Imgproc.threshold(greyInnerWindow, greyInnerWindow, granica, granica + 60, Imgproc.THRESH_BINARY_INV);

		Mat greyCopy = new Mat();
		greyInnerWindow.copyTo(greyCopy);

		Imgproc.findContours(greyCopy, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

		MatOfPoint2f points = new MatOfPoint2f();

		for (int i = 0; i < contours.size(); i++) {
			contours.get(i).convertTo(points, CvType.CV_32FC2);
		}
		Point[] pcontours = points.toArray();
		Point[] forpoint = { new Point(), new Point(), new Point(), new Point() };
		for (int i = 0, c = 0; i < pcontours.length && c < 4; i++) {
			Imgproc.line(rgbaInnerWindow, pcontours[i], pcontours[i], new Scalar(70, 255, 70, 255), 3);
			counter++;
			MidXX += pcontours[i].x;
			MidYY += pcontours[i].y;
		}

		MidX = MidXX / counter;
		MidY = MidYY / counter;
		counter = 0;

		Imgproc.line(rgbaInnerWindow, new Point(0, 383), new Point(0, 0), new Scalar(200, 200, 200, 255), 1);
		Imgproc.line(rgbaInnerWindow, new Point(383, 383), new Point(383, 0), new Scalar(200, 200, 200, 255), 1);
		Imgproc.line(rgbaInnerWindow, new Point(MidX, MidY), new Point(MidX, MidY), new Scalar(255, 70, 70, 255), 5);

		rgbaInnerWindow.release();
		return rgba;
	}

	public void onCameraViewStarted(int width, int height) {
		mIntermediateMat = new Mat();
	}

	public void onCameraViewStopped() {
		if (mIntermediateMat != null)
			mIntermediateMat.release();
		mIntermediateMat = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

	}

	public void ToScreen(String stroka) {
		Toast.makeText(getApplicationContext(), stroka, Toast.LENGTH_SHORT).show();
	}
}
