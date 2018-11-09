# Adnroid OpenCV - Edge Detection




## 작업환경
- Android Studio 3.2.1 (https://developer.android.com/studio/)
- opencv-3.4.3 (https://opencv.org/releases.html)
- opencv-3.4.3-android-sdk (https://sourceforge.net/projects/opencvlibrary/files/opencv-android/)

## AVD(Android Virtual Device) - Image File 업로드 방법
1. AVD 실행
2. Android Stuido > View > Tool Windows > Device File Explorer 선택
3. [Device File Explorer 창] 폴더 목록 > sdcard 폴더 > File 업로드할 폴더 선택후 마우스 오른쪽 클릭 > Upload > 파일 선택 후 OK


## 실행전 수정 필요 - OpenCVEdgeDetection/app/CMakeLists.txt
#### opencv 관련 설정 부분 : 각 경로에 실제 경로 입력 <br/>
- opencv-android-sdk 다운로드 후 해당 경로로 지정 <br/>
set(pathOPENCV <<OPENCV_ANDROID_SDK_경로>>/OpenCV-android-sdk)  
- 프로젝트 경로 <br/>
set(pathPROJECT <<PROJECT_경로>>/OpenCVEdgeDetection)
