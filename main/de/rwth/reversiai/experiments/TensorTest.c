#include "reversitensor.h"
#include "stdlib.h"

int8_t *data;
int32_t width;
int32_t height;
int32_t depth;

JNIEXPORT void JNICALL Java_de_rwth_reversiai_experiments_TensorTest_initNativeFlatArray( JNIEnv *env, jclass class, jint w, jint h, jint d )
{
    width = w;
    height = h;
    depth = d;
    
    data = malloc( width * height * depth );
}

JNIEXPORT jbyte JNICALL Java_de_rwth_reversiai_experiments_TensorTest_getNativeFlatArray( JNIEnv *env, jclass class, jint i, jint j, jint k )
{
    return data[ k + j * depth + i * depth * height ];
}

JNIEXPORT void JNICALL Java_de_rwth_reversiai_experiments_TensorTest_setNativeFlatArray( JNIEnv *env, jclass class, jint i, jint j, jint k, jbyte value )
{
    data[ k + j * depth + i * depth * height ] = value;
}