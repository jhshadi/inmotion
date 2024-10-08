#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <opencv2/objdetect.hpp>
#include <opencv2/objdetect/detection_based_tracker.hpp>

#include <string>
#include <vector>

#include <android/log.h>

#define LOG_TAG "InMotion JNI"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

extern "C" {

	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_AreasDetector_detectMotion(JNIEnv* jenv, jobject, jlong matAddrGr1, jlong matAddrGr2, jlong matAddrRes, jlong matAddrKernel, jboolean isApplayBlur, jint diffThreshhold);
	JNIEXPORT jboolean JNICALL Java_dev_jhshadi_inmotion_detectors_AreasDetector_calcMovementGravity(JNIEnv* jenv, jobject, jlong matAddrRes, jint mLeft, jint mTop, jint mRight, jint mBottom, jint resX, jint resY);
	JNIEXPORT jint JNICALL Java_dev_jhshadi_inmotion_detectors_AreasDetector_checkAreaMovement(JNIEnv* jenv, jobject, jlong matAddrRes, jint mLeft, jint mTop, jint mRight, jint mBottom, jfloat mThresh, jintArray mAvgPoint);

	JNIEXPORT jlong JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeCreateObject(JNIEnv * jenv, jclass, jstring jFileName, jint faceSize);
	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeDestroyObject(JNIEnv * jenv, jclass, jlong thiz);
	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeStart(JNIEnv * jenv, jclass, jlong thiz);
	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeStop(JNIEnv * jenv, jclass, jlong thiz);
	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeSetFaceSize(JNIEnv * jenv, jclass, jlong thiz, jint faceSize);
	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeDetect(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces);


/*	Area Detection	*/

//	JNIEXPORT void JNICALL Java_dev_inmotion_library_AreasDetector_nativeCreateObjects(JNIEnv*, jobject, jlong matAddrGr1, jlong matAddrGr2, jlong matAddrRes, jlong matAddrKernel);
//	JNIEXPORT void JNICALL Java_dev_inmotion_library_AreasDetector_nativeDestroyObjects(JNIEnv*, jobject, jlong matAddrGr1, jlong matAddrGr2, jlong matAddrRes, jlong matAddrKernel);
//
//	JNIEXPORT void JNICALL Java_dev_inmotion_library_AreasDetector_nativeCreateObjects(JNIEnv*, jobject, jlong matAddrGr1, jlong matAddrGr2, jlong matAddrRes, jlong matAddrKernel)
//	{
//		LOGD("Java_dev_inmotion_library_InMotion_nativeCreateObjects enter");
//
//		matAddrGr1		= (jlong)new Mat();
//		matAddrGr2		= (jlong)new Mat();
//		matAddrRes		= (jlong)new Mat();
//		matAddrKernel	= (jlong)new Mat();
//
//
//		LOGD("Java_dev_inmotion_library_InMotion_nativeCreateObjects exit");
//	}
//
//	JNIEXPORT void JNICALL Java_dev_inmotion_library_AreasDetector_nativeDestroyObjects(JNIEnv*, jobject, jlong matAddrGr1, jlong matAddrGr2, jlong matAddrRes, jlong matAddrKernel)
//	{
//		LOGD("Java_dev_inmotion_library_InMotion_nativeDestroyObjects enter");
//
//		if (matAddrGr1 != 0)
//			((Mat*)matAddrGr1)->release();
//		if (matAddrGr2 != 0)
//			((Mat*)matAddrGr2)->release();
//		if (matAddrRes != 0)
//			((Mat*)matAddrRes)->release();
//		if (matAddrKernel != 0)
//			((Mat*)matAddrKernel)->release();
//
//		LOGD("Java_dev_inmotion_library_InMotion_nativeDestroyObjects exit");
//	}

	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_AreasDetector_detectMotion(JNIEnv* jenv, jobject, jlong matAddrGr1, jlong matAddrGr2, jlong matAddrRes, jlong matAddrKernel, jboolean isApplayBlur, jint diffThreshhold)
	{
		//LOGD("Java_dev_inmotion_library_InMotion_detectMotion enter");
		//TODO: set detection only on area and not the whole frame

		Mat& mGr1 = *(Mat*)matAddrGr1;
		Mat& mGr2 = *(Mat*)matAddrGr2;
		Mat& mRes = *(Mat*)matAddrRes;
		Mat& mKernel = *(Mat*)matAddrKernel;

		//Absdiff to get the difference between to the frames
		absdiff(mGr1, mGr2, mRes);

		//Remove the noise and do the threshold
		if (isApplayBlur == true) {
			blur(mRes, mRes, Size(7,7), Point(-1,-1));
		}

		//erode(mRes, mRes, mKernel);
		//dilate(mRes, mRes, mKernel);
		//morphologyEx(mRes, mRes, MORPH_OPEN, mKernel);
		//morphologyEx(mRes, mRes, MORPH_CLOSE, mKernel);
		threshold(mRes, mRes, diffThreshhold, 255, THRESH_BINARY_INV);

		//LOGD("Java_dev_inmotion_library_InMotion_detectMotion exit");
	}

	JNIEXPORT jboolean JNICALL Java_dev_jhshadi_inmotion_detectors_AreasDetector_calcMovementGravity(JNIEnv* jenv, jobject, jlong matAddrRes, jint mLeft, jint mTop, jint mRight, jint mBottom, jint resX, jint resY)
	{
		//LOGD("Java_dev_inmotion_library_AreasDetector_calcMovmentGravity enter");

		Mat& mRes = *(Mat*)matAddrRes;
		uint8_t* pixelPtr = (uint8_t*)mRes.data;
		int cn = mRes.channels();

		bool result = false;
		int nb = 0;
		resX = 0;
    	resY = 0;

    	for (int y = 0; y < mRes.rows; y++) {
        	for (int x = 0; x < mRes.cols; x++) {
        		if (pixelPtr[y*mRes.cols*cn + x*cn + 0] == 0) {
        			resY += y;
        			resX += x;
        			nb++;
        		}
        	}
    	}

    	if (nb != 0) {
    		resX /= nb;
    		resY /= nb;

    		result = true;
    		//circle(mResRGB, Point(avgx, avgy), 10, Scalar(255,0,0,255));
    	}

		//LOGD("Java_dev_inmotion_library_AreasDetector_calcMovmentGravity exit");
		return result;
	}

	JNIEXPORT jint JNICALL Java_dev_jhshadi_inmotion_detectors_AreasDetector_checkAreaMovement(JNIEnv* jenv, jobject, jlong matAddrRes, jint mLeft, jint mTop, jint mRight, jint mBottom, jfloat mThresh, jintArray mAvgPoint)
	{
		//LOGD("Java_dev_inmotion_library_AreasDetector_checkAreasMotion enter");

		Mat& mRes = *(Mat*)matAddrRes;

		uint8_t* pixelPtr = (uint8_t*)mRes.data;
		int cn = mRes.channels();

    	float thresh = 0;
    	int nb = 0;
    	int avg[2] = {0};
    	int result;

    	for (int y = mTop; y <= mBottom; y++) {
        	for (int x = mLeft; x <= mRight; x++) {
        		if (pixelPtr[y*mRes.cols*cn + x*cn + 0] == 0) {
        			avg[1] += y;
        			avg[0] += x;
        			nb++;
        		}
        	}
    	}

    	thresh = (float)(nb * 100) / (float)((mTop - mBottom + 1) * (float)(mLeft - mRight + 1));
    	//LOGD("mResRGB.rows = %d mResRGB.cols = %d", mResRGB.rows, mResRGB.cols);
    	//LOGD("mThresh = %f | thresh = %f", mThresh, thresh);

    	if (mThresh <= thresh) {
    		result =  nb;

    		if (nb != 0) {
    			avg[1] /= nb;
    			avg[0] /= nb;

    			(jenv)->SetIntArrayRegion(mAvgPoint, 0, 2, avg);

        		//circle(mResRGB, Point(avgx, avgy), 10, Scalar(255,0,0,255));
        	}
    	}
    	else {
    		result = -1;
    	}

    	//LOGD("Java_dev_inmotion_library_AreasDetector_checkAreasMotion exit");
    	return result;
	}



/*	Face Detection	*/

	inline void vector_Rect_to_Mat(vector<Rect>& v_rect, Mat& mat)
	{
	    mat = Mat(v_rect, true);
	}

    class CascadeDetectorAdapter: public DetectionBasedTracker::IDetector
    {
        public:
            CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector):
                    IDetector(),
                    Detector(detector)
            {
                LOGD("CascadeDetectorAdapter::Detect::Detect");
                CV_Assert(detector);
            }
            void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects)
            {
                LOGD("CascadeDetectorAdapter::Detect: begin");
                LOGD("CascadeDetectorAdapter::Detect: scaleFactor=%.2f, minNeighbours=%d, minObjSize=(%dx%d), maxObjSize=(%dx%d)", scaleFactor, minNeighbours, minObjSize.width, minObjSize.height, maxObjSize.width, maxObjSize.height);
                Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize, maxObjSize);
                LOGD("CascadeDetectorAdapter::Detect: end");
            }
            virtual ~CascadeDetectorAdapter()
            {
                LOGD("CascadeDetectorAdapter::Detect::~Detect");
            }
        private:
            CascadeDetectorAdapter();
            cv::Ptr<cv::CascadeClassifier> Detector;
    };

    struct DetectorAgregator
    {
        cv::Ptr<CascadeDetectorAdapter> mainDetector;
        cv::Ptr<CascadeDetectorAdapter> trackingDetector;
        cv::Ptr<DetectionBasedTracker> tracker;
        DetectorAgregator(cv::Ptr<CascadeDetectorAdapter>& _mainDetector, cv::Ptr<CascadeDetectorAdapter>& _trackingDetector):
                mainDetector(_mainDetector),
                trackingDetector(_trackingDetector)
        {
            CV_Assert(_mainDetector);
            CV_Assert(_trackingDetector);
            DetectionBasedTracker::Parameters DetectorParams;
            tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, DetectorParams);
        }
    };


	JNIEXPORT jlong JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeCreateObject(JNIEnv * jenv, jclass, jstring jFileName, jint faceSize)
	{
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject enter");
        const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
        string stdFileName(jnamestr);
        jlong result = 0;
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject");
        try
        {
            cv::Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
                    makePtr<CascadeClassifier>(stdFileName));
            cv::Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
                    makePtr<CascadeClassifier>(stdFileName));
            result = (jlong)new DetectorAgregator(mainDetector, trackingDetector);
            if (faceSize > 0)
            {
                mainDetector->setMinObjectSize(Size(faceSize, faceSize));
                //trackingDetector->setMinObjectSize(Size(faceSize, faceSize));
            }
        }
        catch(cv::Exception& e)
        {
            LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
            jclass je = jenv->FindClass("org/opencv/core/CvException");
            if(!je)
                je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, e.what());
        }
        catch (...)
        {
            LOGD("nativeCreateObject caught unknown exception");
            jclass je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeCreateObject()");
            return 0;
        }
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject exit");
        return result;
	}

	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeDestroyObject(JNIEnv * jenv, jclass, jlong thiz)
	{
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDestroyObject");
        try
        {
            if(thiz != 0)
            {
                ((DetectorAgregator*)thiz)->tracker->stop();
                delete (DetectorAgregator*)thiz;
            }
        }
        catch(cv::Exception& e)
        {
            LOGD("nativeestroyObject caught cv::Exception: %s", e.what());
            jclass je = jenv->FindClass("org/opencv/core/CvException");
            if(!je)
                je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, e.what());
        }
        catch (...)
        {
            LOGD("nativeDestroyObject caught unknown exception");
            jclass je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeDestroyObject()");
        }
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDestroyObject exit");
	}

	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeStart(JNIEnv * jenv, jclass, jlong thiz)
	{
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStart");
        try
        {
            ((DetectorAgregator*)thiz)->tracker->run();
        }
        catch(cv::Exception& e)
        {
            LOGD("nativeStart caught cv::Exception: %s", e.what());
            jclass je = jenv->FindClass("org/opencv/core/CvException");
            if(!je)
                je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, e.what());
        }
        catch (...)
        {
            LOGD("nativeStart caught unknown exception");
            jclass je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStart()");
        }
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStart exit");
	}

	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeStop(JNIEnv * jenv, jclass, jlong thiz)
	{
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStop");
        try
        {
            ((DetectorAgregator*)thiz)->tracker->stop();
        }
        catch(cv::Exception& e)
        {
            LOGD("nativeStop caught cv::Exception: %s", e.what());
            jclass je = jenv->FindClass("org/opencv/core/CvException");
            if(!je)
                je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, e.what());
        }
        catch (...)
        {
            LOGD("nativeStop caught unknown exception");
            jclass je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStop()");
        }
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStop exit");
	}

	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeSetFaceSize(JNIEnv * jenv, jclass, jlong thiz, jint faceSize)
	{
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize -- BEGIN");
        try
        {
            if (faceSize > 0)
            {
                ((DetectorAgregator*)thiz)->mainDetector->setMinObjectSize(Size(faceSize, faceSize));
                //((DetectorAgregator*)thiz)->trackingDetector->setMinObjectSize(Size(faceSize, faceSize));
            }
        }
        catch(cv::Exception& e)
        {
            LOGD("nativeStop caught cv::Exception: %s", e.what());
            jclass je = jenv->FindClass("org/opencv/core/CvException");
            if(!je)
                je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, e.what());
        }
        catch (...)
        {
            LOGD("nativeSetFaceSize caught unknown exception");
            jclass je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeSetFaceSize()");
        }
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize -- END");
	}


	JNIEXPORT void JNICALL Java_dev_jhshadi_inmotion_detectors_FaceDetector_nativeDetect(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces)
	{
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDetect");
        try
        {
            vector<Rect> RectFaces;
            ((DetectorAgregator*)thiz)->tracker->process(*((Mat*)imageGray));
            ((DetectorAgregator*)thiz)->tracker->getObjects(RectFaces);
            *((Mat*)faces) = Mat(RectFaces, true);
        }
        catch(cv::Exception& e)
        {
            LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
            jclass je = jenv->FindClass("org/opencv/core/CvException");
            if(!je)
                je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, e.what());
        }
        catch (...)
        {
            LOGD("nativeDetect caught unknown exception");
            jclass je = jenv->FindClass("java/lang/Exception");
            jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
        }
        LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDetect END");
	}
}
