#include <time.h>
#include <android/log.h>

#include "FPSCounter.h"

#define FPSCOUNTER_LOG_TAG    "fpsCounter"
#ifdef DEBUG

#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,FPSCOUNTER_LOG_TAG,__VA_ARGS__)
#else
#define DPRINTF(...)   //noop
#endif
#define IPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,FPSCOUNTER_LOG_TAG,__VA_ARGS__)
#define EPRINTF(...)  __android_log_print(ANDROID_LOG_ERROR,FPSCOUNTER_LOG_TAG,__VA_ARGS__)
#define WPRINTF(...)  __android_log_print(ANDROID_LOG_WARN,FPSCOUNTER_LOG_TAG,__VA_ARGS__)


//------------------------------------------------------------------------------
/// @brief Default constructor
//------------------------------------------------------------------------------
FPSCounter::FPSCounter()
{
   DPRINTF("FPSCounter::FPSCounter");
   Reset();
}

//------------------------------------------------------------------------------
/// @brief Default destructor
//------------------------------------------------------------------------------
FPSCounter::~FPSCounter()
{
   DPRINTF("FPSCounter::~FPSCounter");
}


void FPSCounter::Reset()
{
    struct timeval tv;
    struct timezone tz;

    gettimeofday(&tv, &tz);

    mStartTime   = tv.tv_sec * SECOND + tv.tv_usec;

    mLastTime      = mStartTime;

    mFrameCount    = 0;

    mSecIIR        = 0.;

    mFPS           = 0.;

    mFilteredFPS   = 0.;
}

//------------------------------------------------------------------------------
/// @brief to be called every frame to update the fps count
//------------------------------------------------------------------------------
void FPSCounter::FrameTick()
{
   DPRINTF("FPSCounter::FrameTick");
}