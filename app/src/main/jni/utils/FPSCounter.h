
#ifndef __QCCommon_FPSCOUNTER_HEADERFILE__
#define __QCCommon_FPSCOUNTER_HEADERFILE__

#include <stdint.h>

class FPSCounter
{
    protected:
        /**
        * 1 second defined in terms of micro second.
        */
        static const uint64_t   SECOND = 1000000;

        /**
        * Current FPS
        */
        float                   mFPS;

        /**
        * Filtered FPS
        */
        float                   mFilteredFPS;

        /**
        * Time stamp at start
        */
        uint64_t                mStartTime;

        /**
        * Last time stamp
        */
        uint64_t                mLastTime;

        /**
        * Number of frames elapsed since the start
        */
        uint64_t                mFrameCount;

        /**
        * Filtered average time between measurements.
        */
        float                   mSecIIR;

    public:

   /**
    * @brief  Default Constructor
    */
     FPSCounter();

    /**
     * @brief Default destructor
     *
     */
    ~FPSCounter();

    void Reset();
    void FrameTick();
};

#endif //#ifndef __QCCommon_FPSCOUNTER_HEADERFILE__