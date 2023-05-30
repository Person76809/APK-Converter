import android.content.Intent;

import android.content.pm.ApplicationInfo;

import android.net.Uri;

import android.os.Bundle;

import android.os.Environment;

import android.view.View;

import android.widget.Button;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.FileProvider;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.IOException;

import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private static final String APK_DIRECTORY = Environment.getExternalStorageDirectory() + "/MyAppBackup/";

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button selectAppButton = findViewById(R.id.select_app_button);

        selectAppButton.setOnClickListener(v -> selectApp());

        Button shareButton = findViewById(R.id.share_button);

        shareButton.setOnClickListener(v -> shareApkFile());

    }

    private void selectApp() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("application/vnd.android.package-archive");

        startActivityForResult(intent, REQUEST_CODE);

    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri uri = data.getData();

            if (uri != null) {

                String apkPath = getApkPath(uri);

                if (apkPath != null) {

                    if (backupApkFile(apkPath)) {

                        Toast.makeText(this, "APK file backed up successfully.", Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(this, "Failed to back up APK file.", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(this, "Failed to retrieve APK file.", Toast.LENGTH_SHORT).show();

                }

            }

        }

    }

    private String getApkPath(Uri uri) {

        try {

            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(uri.getEncodedPath(), 0);

            return applicationInfo.sourceDir;

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }

    private boolean backupApkFile(String apkPath) {

        File apkFile = new File(apkPath);

        if (apkFile.exists()) {

            try {

                File backupDirectory = new File(APK_DIRECTORY);

                if (!backupDirectory.exists()) {

                    backupDirectory.mkdirs();

                }

                File backupFile = new File(APK_DIRECTORY, apkFile.getName());

                backupFile.createNewFile();

                FileChannel sourceChannel = null;

                FileChannel destinationChannel = null;

                try {

                    sourceChannel = new FileInputStream(apkFile).getChannel();

                    destinationChannel = new FileOutputStream(backupFile).getChannel();

                    destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

                    return true;

                } catch (IOException e) {

                    e.printStackTrace();

                } finally {

                    if (sourceChannel != null) {

                        sourceChannel.close();

                    }

                    if (destinationChannel != null) {

                        destinationChannel.close();

                    }

                }

            } catch (IOException e) {

                e.printStackTrace();

            }

        }

        return false;

    }

    private void shareApkFile() {

        File backupDirectory = new File(APK_DIRECTORY);

        if (backupDirectory.exists() && backupDirectory.isDirectory()) {

            File[] backupFiles = backupDirectory.listFiles();

                        if (backupFiles != null && backupFiles.length > 0) {

                File apkFile = backupFiles[backupFiles.length - 1];

                Uri apkUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", apkFile);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                shareIntent.setType("application/vnd.android.package-archive");

                shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, "Share APK File"));

            } else {

                Toast.makeText(this, "No APK files found for sharing.", Toast.LENGTH_SHORT).show();

            }

        } else {

            Toast.makeText(this, "APK backup directory not found.", Toast.LENGTH_SHORT).show();

        }

    }

}

