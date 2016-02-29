package com.wq.photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 调用媒体选择库 需要在inten中传递2个参数 1. 选择模式 chose_mode 0 //单选 1多选 2. 选择张数 max_chose_count
 * 多选模式默认 9 张
 */
public class MediaChoseActivity extends FragmentActivity {

    public static final int CHOSE_MODE_SINGLE = 0;
    public static final int CHOSE_MODE_MULTIPLE = 1;
    public int max_chose_count = 1;
    public LinkedHashMap imasgemap = new LinkedHashMap();
    public LinkedHashSet imagesChose = new LinkedHashSet();
    PhotoGalleryFragment photoGalleryFragment;
    int chosemode = CHOSE_MODE_MULTIPLE;

    boolean isneedCrop = false;
    int crop_image_w, crop_image_h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_chose);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        chosemode = getIntent().getIntExtra("chose_mode", CHOSE_MODE_MULTIPLE);
        if (chosemode == CHOSE_MODE_MULTIPLE) {
            max_chose_count = getIntent().getIntExtra("max_chose_count", 9);// 不建议超过9
        }

        // 是否需要剪裁
        isneedCrop = getIntent().getBooleanExtra("crop", false);
        if (isneedCrop) {
            chosemode = CHOSE_MODE_SINGLE;
            max_chose_count = 1;
            crop_image_w = getIntent().getIntExtra("crop_image_w", 720);
            crop_image_h = getIntent().getIntExtra("crop_image_h", 720);
        }
        photoGalleryFragment = PhotoGalleryFragment.newInstance(chosemode, max_chose_count);
        fragmentTransaction.add(R.id.container, photoGalleryFragment, PhotoGalleryFragment.class.getSimpleName());
        fragmentTransaction.commit();

        initHeadBar();
    }

    boolean isPriview = false;
    private TextView mPhotoCountTextView;
    private RelativeLayout rootView;
    private TextView mBack;
    private TextView mDelete;
    private TextView mSend;
    private TextView mBackText;
    private RelativeLayout rllayout;

    @SuppressLint("NewApi")
    public void starPriview(LinkedHashMap map, String currentimage) {
        if (isneedCrop && !isCropOver) {
            sendStarCrop(currentimage);
        } else {
            Set<String> keys = map.keySet();
            ArrayList<String> ims = new ArrayList<>();
            int pos = 0;
            int i = 0;
            for (String s : keys) {
                ims.add((String) map.get(s));
                if (map.get(s).equals(currentimage)) {
                    pos = i;
                }
                i++;
            }
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container, ImagePreviewFragemnt.newInstance(ims, pos), ImagePreviewFragemnt.class.getSimpleName());
            fragmentTransaction.addToBackStack("con");
            fragmentTransaction.commit();
            isPriview = true;

            switchALLUI();
        }
    }

    public void switchDeleteButton() {
        if (isPriview && (chosemode == CHOSE_MODE_MULTIPLE)) {
            mDelete.setEnabled(true);
            mDelete.setVisibility(View.VISIBLE);
        } else {
            mDelete.setEnabled(false);
            mDelete.setVisibility(View.INVISIBLE);
        }
    }

    public Fragment getCurrentFragment(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public LinkedHashMap getImageChoseMap() {
        return imasgemap;
    }

  

    public void initHeadBar() {
        rootView = (RelativeLayout) findViewById(R.id.title_bar_picker);
        mBack = (TextView) rootView.findViewById(R.id.title_bar_picker_left_back);
        mBackText = (TextView) rootView.findViewById(R.id.title_bar_picker_left_back_text);
        mDelete = (TextView) rootView.findViewById(R.id.title_bar_picker_right_delete);
        mSend = (TextView) rootView.findViewById(R.id.title_bar_picker_right_send);
        mBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popFragment();
            }
        });
        mBackText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popFragment();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ImagePreviewFragemnt fragemnt = (ImagePreviewFragemnt) getCurrentFragment(ImagePreviewFragemnt.class.getSimpleName());
                if (fragemnt != null) {
                    String img = fragemnt.delete();
                    Iterator iterator = imasgemap.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        if (imasgemap.get(key).equals(img)) {
                            iterator.remove();
                        }
                    }
                    switchALLUI();
                }
            }
        });
        mSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendImages();
            }
        });

        switchALLUI();
    }

    public void switchALLUI() {
        switchDeleteButton();
        switchSendButton();
    }

    public void switchSendButton() {
        if (imasgemap == null) {
            return;
        }
        if (imasgemap.size() < 1) {
            mSend.setEnabled(false);
            mSend.setVisibility(View.INVISIBLE);
        } else {
            mSend.setEnabled(true);
            mSend.setVisibility(View.VISIBLE);
            if (chosemode == CHOSE_MODE_MULTIPLE) {
                mSend.setText("确认(" + imasgemap.size() + "/" + max_chose_count + ")");
            } else {
                mSend.setText("确认(1)");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FragmentManager fm = getSupportFragmentManager();
        if (keyCode == KeyEvent.KEYCODE_BACK && fm.getBackStackEntryCount() > 0) {
            popFragment();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void log(String msg) {
        Log.i("gallery", msg);
    }

    @SuppressLint("NewApi")
    public void popFragment() {

        ImagePreviewFragemnt fragemnt = (ImagePreviewFragemnt) getCurrentFragment(ImagePreviewFragemnt.class.getSimpleName());
        if (fragemnt != null && fragemnt.isVisible()) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStackImmediate();
            isPriview = false;

            switchALLUI();
            if (photoGalleryFragment != null && chosemode == CHOSE_MODE_MULTIPLE) {
                photoGalleryFragment.notifyDataSetChanged();
            }
        } else {
            // 已经是组件首页
            finish();
        }
    }

    boolean isCropOver = false;

    public void sendImages() {
        if (isneedCrop && !isCropOver) {
            Iterator iterator = imasgemap.keySet().iterator();
            File file = new File(iterator.next().toString());
            if (!file.exists()) {
                Toast.makeText(this, "获取文件失败", Toast.LENGTH_SHORT).show();
            }
            sendStarCrop(file.getAbsolutePath());
        } else {
            Intent intent = new Intent();
            ArrayList<String> img = new ArrayList<>();
            Iterator iterator = imasgemap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                img.add((String) imasgemap.get(key));
            }
            intent.putExtra("data", img);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CROP && (chosemode == CHOSE_MODE_SINGLE)) {
            Intent intent = new Intent();
            ArrayList<String> img = new ArrayList<>();
            String crop_path = data.getStringExtra("crop_path");
            isCropOver = true;
            if (crop_path != null && new File(crop_path) != null) {
                img.add(crop_path);
                intent.putExtra("data", img);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "截取剪图失败", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA && (chosemode == CHOSE_MODE_SINGLE)) {
            if (currentfile != null && currentfile.exists() && currentfile.length() > 10) {
                if (isneedCrop && !isCropOver) {
                    sendStarCrop(currentfile.getAbsolutePath());
                } else {
                    Intent intent = new Intent();
                    ArrayList<String> img = new ArrayList<>();
                    img.add(currentfile.getAbsolutePath());
                    intent.putExtra("data", img);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                insertImage(currentfile.getAbsolutePath());
            } else {
                Toast.makeText(MediaChoseActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA && (chosemode == CHOSE_MODE_MULTIPLE)) {
            if (currentfile != null && currentfile.exists() && currentfile.length() > 10) {
                getImageChoseMap().put(currentfile.getAbsolutePath(), currentfile.getAbsolutePath());
                switchALLUI();
                insertImage(currentfile.getAbsolutePath());
            } else {
                Toast.makeText(MediaChoseActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void insertImage(String fileName) {
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), fileName, new File(fileName).getName(), new File(fileName).getName());
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(fileName));
            intent.setData(uri);
            sendBroadcast(intent);
            MediaScannerConnection.scanFile(this, new String[] { fileName }, new String[] { "image/jpeg" },
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            photoGalleryFragment.addCaptureFile(path);
                        }
                    });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static final int REQUEST_CODE_CAMERA = 2001;
    public static final int REQUEST_CODE_CROP = 2002;
    File currentfile;

    public void sendStarCamera() {
        currentfile = getTempFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentfile));
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    public void sendStarCrop(String path) {
        Intent intent = new Intent(this, CropImageActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        intent.putExtra("crop_image_w", crop_image_w);
        intent.putExtra("crop_image_h", crop_image_h);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getCropFile().getAbsolutePath());
        startActivityForResult(intent, REQUEST_CODE_CROP);
    }

    public File getTempFile() {
        String str = null;
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        date = new Date(System.currentTimeMillis());
        str = format.format(date);
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "__taojinjia__" + str + ".jpg");
    }

    public File getCropFile() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ".crop.jpg");
    }

}
