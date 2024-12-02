# Video Trimming App with Custom Two-Sided Slide Range Bar

![Custom Two-Sided Slide Range Bar](./Screenshot%202024-12-02%20194756.png)


This project is a video trimming application that allows users to trim videos by selecting a range with a custom two-sided slide range bar. It uses Android’s `MediaMetadataRetriever` to extract frames from videos for visual previewing.

## Features

- **Custom Two-Sided Slide Range Bar**: Users can select a range in the video for trimming.
- **Frame Extraction**: Extracts 10 frames from the video at regular intervals.
- **Video Preview**: Provides a frame-by-frame preview of the video for more precise trimming.

## How It Works

### Frame Extraction

The video frames are extracted using the `MediaMetadataRetriever` class, which allows for frame retrieval at specific intervals. The method below demonstrates how frames are extracted from a video.

### Code Example:

```kotlin
private fun extractFrames(videoUri: Uri) {
    val retriever = android.media.MediaMetadataRetriever()
    retriever.setDataSource(this, videoUri)

    // Get video duration
    videoDuration =
        retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLong() ?: 0L
    frameInterval = videoDuration / 10 // Example: 10 frames

    // Clear any previous frames
    frameList.clear()
    for (i in 0 until 10) {
        val frameTimeUs = i * frameInterval * 1000
        val bitmap = retriever.getFrameAtTime(frameTimeUs, android.media.MediaMetadataRetriever.OPTION_CLOSEST)
        if (bitmap != null) {
            frameList.add(bitmap)
        }
    }
    retriever.release()

    // Set frames to the custom range slider
    rangeSliderCustom.setVideoFrames(frameList)
}
