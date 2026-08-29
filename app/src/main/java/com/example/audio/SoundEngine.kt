package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sampleRate = 44100
    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Pre-rendered cached sound buffers
    private val gnuGnuBuffers = mutableListOf<ByteArray>()
    private var blasterBuffer: ByteArray? = null
    private var laserHeavyBuffer: ByteArray? = null
    private var zapBuffer: ByteArray? = null
    private var hitBuffer: ByteArray? = null
    private var explosionBuffer: ByteArray? = null
    private var powerupBuffer: ByteArray? = null
    private var bombBuffer: ByteArray? = null
    private var superchargeBuffer: ByteArray? = null
    private var victoryBuffer: ByteArray? = null

    init {
        scope.launch {
            // Generate multiple hilarious Gnugnu variations
            gnuGnuBuffers.add(generateGnuGnuPcm(basePitch = 380.0, speed = 1.0))
            gnuGnuBuffers.add(generateGnuGnuPcm(basePitch = 480.0, speed = 1.2))
            gnuGnuBuffers.add(generateGnuGnuPcm(basePitch = 300.0, speed = 0.9))
            gnuGnuBuffers.add(generateGnuGnuPcm(basePitch = 600.0, speed = 1.4))

            blasterBuffer = generateBlasterPcm()
            laserHeavyBuffer = generateLaserHeavyPcm()
            zapBuffer = generateZapPcm()
            hitBuffer = generateHitPcm()
            explosionBuffer = generateExplosionPcm()
            powerupBuffer = generatePowerUpPcm()
            bombBuffer = generateBombPcm()
            superchargeBuffer = generateSuperchargePcm()
            victoryBuffer = generateVictoryPcm()
        }
    }

    /**
     * Synthesizes the iconic comical "Gnu-Gnu!" two-part cartoon voice!
     */
    private fun generateGnuGnuPcm(basePitch: Double, speed: Double): ByteArray {
        val sylDuration = (0.13 / speed)
        val gapDuration = (0.05 / speed)
        val totalDuration = (sylDuration * 2) + gapDuration
        val totalSamples = (sampleRate * totalDuration).toInt()
        val pcm = ShortArray(totalSamples)

        val syl1Samples = (sampleRate * sylDuration).toInt()
        val gapSamples = (sampleRate * gapDuration).toInt()

        // First "Gnu"
        for (i in 0 until syl1Samples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / syl1Samples
            val freq = basePitch * (0.85 + 0.65 * progress)
            val f1 = sin(2.0 * PI * freq * t)
            val f2 = 0.5 * sin(2.0 * PI * (freq * 2.2) * t)
            val f3 = 0.25 * sin(2.0 * PI * (freq * 3.5) * t)
            val wave = f1 + f2 + f3

            val env = if (progress < 0.2) progress / 0.2 else (1.0 - progress).coerceAtLeast(0.0)
            pcm[i] = (wave * env * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }

        // Second "Gnu!"
        val secondPitch = basePitch * 1.35
        for (i in 0 until syl1Samples) {
            val sampleIdx = syl1Samples + gapSamples + i
            if (sampleIdx >= totalSamples) break
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / syl1Samples
            val freq = secondPitch * (1.0 + 0.8 * progress)
            val f1 = sin(2.0 * PI * freq * t)
            val f2 = 0.6 * sin(2.0 * PI * (freq * 2.1) * t)
            val f3 = 0.3 * sin(2.0 * PI * (freq * 3.2) * t)
            val wave = f1 + f2 + f3

            val env = if (progress < 0.15) progress / 0.15 else (1.0 - progress).coerceAtLeast(0.0)
            pcm[sampleIdx] = (wave * env * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }

        return shortArrayToByteArray(pcm)
    }

    private fun generateBlasterPcm(): ByteArray {
        val duration = 0.08
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 1200.0 * (1.0 - progress * 0.7)
            val wave = sin(2.0 * PI * freq * (i.toDouble() / sampleRate))
            val env = (1.0 - progress) * (1.0 - progress)
            pcm[i] = (wave * env * 18000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generateLaserHeavyPcm(): ByteArray {
        val duration = 0.14
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 1600.0 * (1.0 - progress * 0.8)
            val wave = sin(2.0 * PI * freq * (i.toDouble() / sampleRate)) + 0.3 * sin(2.0 * PI * (freq * 2) * (i.toDouble() / sampleRate))
            val env = 1.0 - progress
            pcm[i] = (wave * env * 20000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generateZapPcm(): ByteArray {
        val duration = 0.10
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        val rnd = Random(42)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val noise = (rnd.nextDouble() * 2.0 - 1.0) * 0.4
            val tone = sin(2.0 * PI * 850.0 * (i.toDouble() / sampleRate))
            val env = (1.0 - progress)
            pcm[i] = ((tone + noise) * env * 16000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generateHitPcm(): ByteArray {
        val duration = 0.10
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 320.0 * (1.0 - progress * 0.5)
            val wave = sin(2.0 * PI * freq * (i.toDouble() / sampleRate))
            val env = exp(-progress * 8.0)
            pcm[i] = (wave * env * 20000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generateExplosionPcm(): ByteArray {
        val duration = 0.28
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        val rnd = Random(123)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val noise = (rnd.nextDouble() * 2.0 - 1.0)
            val lowBoom = sin(2.0 * PI * 90.0 * (1.0 - progress * 0.5) * (i.toDouble() / sampleRate))
            val env = (1.0 - progress) * (1.0 - progress)
            pcm[i] = ((noise * 0.6 + lowBoom * 0.4) * env * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generatePowerUpPcm(): ByteArray {
        val duration = 0.25
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 400.0 + progress * 800.0
            val wave = sin(2.0 * PI * freq * (i.toDouble() / sampleRate))
            val env = sin(PI * progress)
            pcm[i] = (wave * env * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generateBombPcm(): ByteArray {
        val duration = 0.6
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        val rnd = Random(999)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val noise = (rnd.nextDouble() * 2.0 - 1.0)
            val sweep = sin(2.0 * PI * (250.0 * (1.0 - progress * 0.7)) * (i.toDouble() / sampleRate))
            val env = (1.0 - progress)
            pcm[i] = ((noise * 0.7 + sweep * 0.5) * env * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generateSuperchargePcm(): ByteArray {
        val duration = 0.35
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 200.0 + (progress * progress) * 1200.0
            val wave = sin(2.0 * PI * freq * (i.toDouble() / sampleRate))
            val env = sin(PI * progress)
            pcm[i] = (wave * env * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun generateVictoryPcm(): ByteArray {
        val duration = 0.5
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)
        val chordFreqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples
            var wave = 0.0
            for (f in chordFreqs) {
                wave += sin(2.0 * PI * f * t)
            }
            wave /= chordFreqs.size
            val env = (1.0 - progress)
            pcm[i] = (wave * env * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortArrayToByteArray(pcm)
    }

    private fun shortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(shortArray.size * 2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        for (s in shortArray) {
            byteBuffer.putShort(s)
        }
        return byteBuffer.array()
    }

    fun playGnuGnu() {
        if (!isSoundEnabled) return
        triggerHaptic(HapticType.HIT)
        scope.launch {
            if (gnuGnuBuffers.isNotEmpty()) {
                val index = Random.nextInt(gnuGnuBuffers.size)
                playPcmBuffer(gnuGnuBuffers[index])
            }
        }
    }

    fun playShoot() {
        if (!isSoundEnabled) return
        scope.launch {
            blasterBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playLaserHeavy() {
        if (!isSoundEnabled) return
        scope.launch {
            laserHeavyBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playZap() {
        if (!isSoundEnabled) return
        scope.launch {
            zapBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playHit() {
        if (!isSoundEnabled) return
        triggerHaptic(HapticType.LIGHT)
        scope.launch {
            hitBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playExplosion() {
        if (!isSoundEnabled) return
        triggerHaptic(HapticType.EXPLOSION)
        scope.launch {
            explosionBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playPowerUp() {
        if (!isSoundEnabled) return
        triggerHaptic(HapticType.SUCCESS)
        scope.launch {
            powerupBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playBomb() {
        if (!isSoundEnabled) return
        triggerHaptic(HapticType.EXPLOSION)
        scope.launch {
            bombBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playSupercharge() {
        if (!isSoundEnabled) return
        triggerHaptic(HapticType.SUCCESS)
        scope.launch {
            superchargeBuffer?.let { playPcmBuffer(it) }
        }
    }

    fun playVictory() {
        if (!isSoundEnabled) return
        triggerHaptic(HapticType.SUCCESS)
        scope.launch {
            victoryBuffer?.let { playPcmBuffer(it) }
        }
    }

    private fun playPcmBuffer(pcmData: ByteArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcmData.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.play()
            scope.launch {
                val durationMs = (pcmData.size / 2 * 1000L) / sampleRate + 50L
                kotlinx.coroutines.delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    private fun triggerHaptic(type: HapticType) {
        if (!isHapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.HIT -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.EXPLOSION -> VibrationEffect.createWaveform(longArrayOf(0, 50, 30, 80), -1)
                    HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 30, 20, 50), -1)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.LIGHT -> vibrator?.vibrate(15)
                    HapticType.HIT -> vibrator?.vibrate(35)
                    HapticType.EXPLOSION -> vibrator?.vibrate(120)
                    HapticType.SUCCESS -> vibrator?.vibrate(80)
                }
            }
        } catch (_: Exception) {}
    }

    enum class HapticType {
        LIGHT,
        HIT,
        EXPLOSION,
        SUCCESS
    }
}
