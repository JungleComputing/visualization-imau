#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "../../sage-code/sage3.0/include/libsage.h"

#include "imau_visualization_jni_SageInterface.h"

sail *sageInf; // sail object
void *buffer;

int resX, resY;
double frames;

int started = 0;

//Attach the current thread to the JVM if it hasn't been done yet
	//JavaVM *jvm;
	//env->GetJavaVM(&jvm);
	//int res = jvm->AttachCurrentThread((void **)&env, NULL);
	//if (res < 0) {
	//	fprintf(stderr, "Attach failed\n");
	//	return false;
	//}

JNIEXPORT jint JNICALL Java_imau_visualization_jni_SageInterface_setup(JNIEnv *env, jobject obj, jint width, jint height, jint fps) {
	resX = (int)width;
	resY = (int)height;
	frames = (double)fps;

	sageInf = createSAIL("javaBridge", resX, resY, PIXFMT_8888_INV, NULL, TOP_TO_BOTTOM, frames);
	
	buffer = nextBuffer(sageInf);



	return 0;
}

JNIEXPORT jint JNICALL Java_imau_visualization_jni_SageInterface_start(JNIEnv *env, jobject obj, jintArray arr, jint size) {
	//long *int1;
	//int1 = (long *)malloc(sizeof(long)*size);
	//env->GetIntArrayRegion(arr,0,size,int1);
	//free(int1);

	jint *body = env->GetIntArrayElements(arr, 0);

	memcpy(buffer, body, resX*resY*4);

	//env->GetIntArrayRegion(arr, 0, size, (int*)buffer);
	//memset(buffer, 100, resX*resY*4);
	buffer = swapAndNextBuffer(sageInf);

	processMessages(sageInf);

	return 0;
}

