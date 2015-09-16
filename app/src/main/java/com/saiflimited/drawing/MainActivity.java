package com.saiflimited.drawing;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnClickListener {

    private final String tag = "MainActivity";
    int currentApiVersion;

    boolean exit = false;
    private ImageView eraser;
    private Button btnChooseImage;
    private ImageButton btnClear, btnSave, btnShare, btnCamera;

    private DrawingView drawingView;

    private static final int SELECT_PHOTO = 100;
    private static final int CAMERA_REQUEST = 1888;
    public static int CURRENT_BRUSH_SIZE;
    public static String CURRENT_BRUSH_COLOR;
    List<String> listOfColors;
    List<Integer> listOfBrushes;
    int countForBrush = 0;
    int countForColor = 0;
    RelativeLayout layoutForColor;
    TextView layoutForSize;
    boolean flag = false;
    SimpleDateFormat format;
    boolean flag2 = false;
    int countOfClicks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (savedInstanceState == null) {

            listOfColors = new ArrayList<String>();
            listOfColors.add("#010101");
            listOfColors.add("#110101");
            listOfColors.add("#012101");
            listOfColors.add("#0144FF");
            listOfColors.add("#FF0101");
            listOfColors.add("#01FF01");
            listOfColors.add("#FF0100");
            listOfColors.add("#0101FF");
            listOfColors.add("#039901");
            listOfColors.add("#0FF101");

            listOfBrushes = new ArrayList<Integer>();
            listOfBrushes.add(2);
            listOfBrushes.add(4);
            listOfBrushes.add(6);
            listOfBrushes.add(8);
            listOfBrushes.add(10);

            CURRENT_BRUSH_COLOR = listOfColors.get(countForColor);
            CURRENT_BRUSH_SIZE = listOfBrushes.get(countForBrush);
            setContentView(R.layout.activity_main);

            exit = false;
            currentApiVersion = android.os.Build.VERSION.SDK_INT;

            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            // This work only for android 4.4+
            if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

                getWindow().getDecorView().setSystemUiVisibility(flags);

                // Code below is to handle presses of Volume up or Volume down.
                // Without this, after pressing volume buttons, the navigation bar will
                // show up and won't hide
                final View decorView = getWindow().getDecorView();
                decorView
                        .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                            @Override
                            public void onSystemUiVisibilityChange(int visibility) {
                                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                    decorView.setSystemUiVisibility(flags);
                                }
                            }
                        });
            }
            WindowManager manager = ((WindowManager) getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));

            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                    // this is to enable the notification to recieve touch events
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                    // Draws over status bar
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = (int) (50 * getResources()
                    .getDisplayMetrics().scaledDensity);
            localLayoutParams.format = PixelFormat.TRANSPARENT;

            customViewGroup view = new customViewGroup(this);

            WindowManager.LayoutParams localLayoutParamsForRightSide = new WindowManager.LayoutParams();
            localLayoutParamsForRightSide.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            localLayoutParamsForRightSide.gravity = Gravity.RIGHT;
            localLayoutParamsForRightSide.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                    // this is to enable the notification to recieve touch events
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                    // Draws over status bar
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            localLayoutParamsForRightSide.width = (int) (20 * getResources()
                    .getDisplayMetrics().scaledDensity);
            localLayoutParamsForRightSide.height = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParamsForRightSide.format = PixelFormat.TRANSPARENT;

            customViewGroup viewForRight = new customViewGroup(this);

            manager.addView(view, localLayoutParams);
            //manager.addView(viewForRight,localLayoutParamsForRightSide);

            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
            lock.disableKeyguard();

            format = new SimpleDateFormat("MMddyyyyHHmmss");
            layoutForColor = (RelativeLayout) findViewById(R.id.brushcolor);
            layoutForColor.setBackgroundColor(Color.parseColor(CURRENT_BRUSH_COLOR));
            layoutForSize = (TextView) findViewById(R.id.brushsize);
            layoutForSize.setText(CURRENT_BRUSH_SIZE + "");


            layoutForSize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    System.out.println("CICKED");
                    if (countOfClicks == 0) {

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                countOfClicks = 0;
                                System.out.println("COUNT___0");
                            }
                        }, 5000);
                    } else if (countOfClicks > 5) {

                        exitApp();
                    }
                    countOfClicks++;


                }
            });
            drawingView = (DrawingView) findViewById(R.id.drawing);

            btnChooseImage = (Button) findViewById(R.id.btnChooseImage);
            btnChooseImage.setOnClickListener(this);

            btnClear = (ImageButton) findViewById(R.id.btnClear);
            btnClear.setOnClickListener(this);

            btnSave = (ImageButton) findViewById(R.id.btnSave);
            btnSave.setOnClickListener(this);

            btnShare = (ImageButton) findViewById(R.id.btnShare);
            btnShare.setOnClickListener(this);

            btnCamera = (ImageButton) findViewById(R.id.btnCamera);
            btnCamera.setOnClickListener(this);

            eraser = (ImageView) findViewById(R.id.eraser);
            eraser.setOnClickListener(this);

        }



    }


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_main, menu);
//		return true;
//	}


    @Override
    public void onClick(View v) {

        if (v == eraser) {

            if (drawingView.isEraserActive()) {

                drawingView.deactivateEraser();

                eraser.setImageResource(R.drawable.eraser);

            } else {

                drawingView.activateEraser();

                eraser.setImageResource(R.drawable.pencil);
            }

        } else if (v == btnClear) {

            drawingView.reset();
            drawingView.setBackground(null);

        } else if (v == btnSave) {

            saveImage();

        } else if (v == btnCamera) {

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);

        } else if (v == btnShare) {

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/png");

            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(saveImage().getAbsolutePath())); //"file:///sdcard/temporary_file.jpg"
            startActivity(Intent.createChooser(share, "Share Image"));

        } else if (v == btnChooseImage) {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);

        }

    }

    public void exitApp() {
        exit = true;
        getPackageManager().clearPackagePreferredActivities(getPackageName());
        finish();
    }

    public File saveImage() {
        drawingView.setDrawingCacheEnabled(true);
        Bitmap bm = drawingView.getDrawingCache();

        File fPath = new File(Environment.getExternalStorageDirectory(), "Drawings");
        if (!fPath.exists()) {
            fPath.mkdir();
        }
        File f = null;

        f = new File(fPath, format.format(new Date()) + ".png");

        try {
            FileOutputStream strm = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 80, strm);
            strm.close();
            addImageToGallery(f.getPath(), this);
            Toast.makeText(getApplicationContext(), "Image is saved successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {

            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);

                        if (Build.VERSION.SDK_INT >= 16) {
                            drawingView.setBackground(ob);
                        } else {
                            drawingView.setBackgroundDrawable(ob);
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {

                    Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");

                    BitmapDrawable ob = new BitmapDrawable(getResources(), photo);

                    if (Build.VERSION.SDK_INT >= 16) {
                        drawingView.setBackground(ob);
                    } else {
                        drawingView.setBackgroundDrawable(ob);
                    }
                }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
                    drawingView.reset();
                    drawingView.setBackground(null);
                    return true;
                }
                if (action == KeyEvent.ACTION_UP) {
                    volumeUp();
                }
                return true;
            case KeyEvent.KEYCODE_POWER:
                finish();
                return true;

            default:
                return super.dispatchKeyEvent(event);
        }
    }


    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {


        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d("Test", "Long press!");
            flag = false;
            flag2 = true;
            saveImage();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // Do something here...
            finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int action = event.getAction();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            event.startTracking();
            if (flag2 == true) {
                flag = false;
            } else {
                flag = true;
                flag2 = false;
            }

            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // Do something here...

            event.startTracking();
            finish();// Needed to track long presses
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            event.startTracking();
            if (flag) {
                Log.d("Test", "Short");
                volumeDown();
            }
            flag = true;
            flag2 = false;
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }


    public void volumeUp() {
        countForColor++;
        if (countForColor >= listOfColors.size()) {
            countForColor = 0;
        }
        CURRENT_BRUSH_COLOR = listOfColors.get(countForColor);
        //drawingView.onCanvasInitialization();
        layoutForColor.setBackgroundColor(Color.parseColor(CURRENT_BRUSH_COLOR));
    }

    public void volumeDown() {
        countForBrush++;
        if (countForBrush >= listOfBrushes.size()) {
            countForBrush = 0;
        }
        CURRENT_BRUSH_SIZE = listOfBrushes.get(countForBrush);
        // drawingView.onCanvasInitialization();
        layoutForSize.setText(CURRENT_BRUSH_SIZE + "");

    }

    public class customViewGroup extends ViewGroup {

        public customViewGroup(Context context) {
            super(context);
        }


        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v("customViewGroup", "**********Intercepted");
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
        System.out.println("back pressed");
        //finish();
    }

    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub


        System.out.println("before call");
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        System.out.println("after call");
        super.onAttachedToWindow();

    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!exit)
        {
            startActivity(getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        }
        else
        {

        }

    }

}
