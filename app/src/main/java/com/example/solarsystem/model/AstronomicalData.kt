package com.example.solarsystem.model

import com.example.solarsystem.graphics.Matrix4
import com.example.solarsystem.graphics.Vector3
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RingBand(
    val innerFactor: Float,
    val outerFactor: Float,
    val color: Long,
    val alpha: Float
)

data class MoonData(
    val name: String,
    val periodDays: Float,
    val dist: Float
)

data class PlanetData(
    val key: String,
    val color: Long,
    val radius: Float,
    val a: Float,
    val e: Float,
    val i: Float,
    val periodDays: Float,
    val moonsCount: Int,
    val m0: Float,
    val w: Float,
    val omega: Float,
    val tiltDeg: Float = 0f,
    val hasRings: Boolean = false,
    val rings: List<RingBand> = emptyList(),
    val accurateMoons: List<MoonData> = emptyList()
)

data class NearbyStar(
    val key: String,
    val color: Long,
    val radius: Float,
    val dist: Float,
    val angle: Float,
    val height: Float
)

data class ConstellationData(
    val key: String,
    val dist: Float,
    val angle: Float,
    val height: Float,
    val pointsOffsets: List<FloatArray>
)

object AstronomicalData {
    const val SUN_GALACTIC_RADIUS = 2500f
    const val WAVES_PER_ORBIT = 6f
    const val WAVE_AMPLITUDE = 180f
    const val GALACTIC_ORBIT_DAYS = 365.25 * 225000000.0

    val planets: List<PlanetData> = listOf(
        PlanetData(
            key = "mercury", color = 0xFF9E9E9E, radius = 1.5f, a = 25f, e = 0.2056f, i = 7.00f,
            periodDays = 87.969f, moonsCount = 0, m0 = 174.80f, w = 29.12f, omega = 48.33f
        ),
        PlanetData(
            key = "venus", color = 0xFFE6A845, radius = 2.2f, a = 38f, e = 0.0067f, i = 3.39f,
            periodDays = 224.701f, moonsCount = 0, m0 = 50.44f, w = 54.85f, omega = 76.68f
        ),
        PlanetData(
            key = "earth", color = 0xFF3B82F6, radius = 2.5f, a = 55f, e = 0.0167f, i = 0.00f,
            periodDays = 365.256f, moonsCount = 1, m0 = 357.52f, w = 114.20f, omega = 0.0f,
            tiltDeg = 23.5f,
            accurateMoons = listOf(MoonData("Moon", 27.322f, 4.0f))
        ),
        PlanetData(
            key = "mars", color = 0xFFEF4444, radius = 2.0f, a = 75f, e = 0.0934f, i = 1.85f,
            periodDays = 686.980f, moonsCount = 2, m0 = 19.41f, w = 286.47f, omega = 49.57f,
            tiltDeg = 25.2f,
            accurateMoons = listOf(MoonData("Phobos", 0.3189f, 1.5f), MoonData("Deimos", 1.262f, 3.0f))
        ),
        PlanetData(
            key = "ceres", color = 0xFF888888, radius = 1.0f, a = 95f, e = 0.076f, i = 10.59f,
            periodDays = 1680.0f, moonsCount = 0, m0 = 77.4f, w = 73.6f, omega = 80.3f
        ),
        PlanetData(
            key = "jupiter", color = 0xFFC2A578, radius = 6.5f, a = 120f, e = 0.0483f, i = 1.30f,
            periodDays = 4332.59f, moonsCount = 4, m0 = 19.65f, w = 274.20f, omega = 100.55f,
            tiltDeg = 3.1f, hasRings = true,
            rings = listOf(RingBand(1.3f, 1.8f, 0xFF998877, 0.25f)),
            accurateMoons = listOf(
                MoonData("Io", 1.769f, 3.0f),
                MoonData("Europa", 3.551f, 5.0f),
                MoonData("Ganymede", 7.154f, 8.0f),
                MoonData("Callisto", 16.689f, 12.0f)
            )
        ),
        PlanetData(
            key = "saturn", color = 0xFFE3E0B8, radius = 5.5f, a = 170f, e = 0.0541f, i = 2.48f,
            periodDays = 10759.22f, moonsCount = 3, m0 = 317.51f, w = 338.72f, omega = 113.71f,
            tiltDeg = 26.7f, hasRings = true,
            rings = listOf(
                RingBand(1.2f, 1.5f, 0xFF8C7C68, 0.45f),
                RingBand(1.51f, 1.95f, 0xFFCBC5A7, 0.85f),
                RingBand(1.98f, 2.25f, 0xFFA8A38C, 0.60f),
                RingBand(2.28f, 2.32f, 0xFF888899, 0.40f)
            ),
            accurateMoons = listOf(
                MoonData("Titan", 15.945f, 8.0f),
                MoonData("Enceladus", 1.370f, 3.5f),
                MoonData("Mimas", 0.942f, 2.2f)
            )
        ),
        PlanetData(
            key = "uranus", color = 0xFF67E8F9, radius = 4.0f, a = 220f, e = 0.0471f, i = 0.77f,
            periodDays = 30685.4f, moonsCount = 2, m0 = 142.27f, w = 96.74f, omega = 74.22f,
            tiltDeg = 97.8f, hasRings = true,
            rings = listOf(
                RingBand(1.5f, 1.55f, 0xFFAAAAAA, 0.35f),
                RingBand(1.6f, 1.65f, 0xFFBBBBBB, 0.45f),
                RingBand(1.9f, 2.0f, 0xFF88CCDD, 0.65f)
            ),
            accurateMoons = listOf(MoonData("Titania", 8.706f, 4.0f), MoonData("Oberon", 13.46f, 6.0f))
        ),
        PlanetData(
            key = "neptune", color = 0xFF3B82F6, radius = 4.0f, a = 260f, e = 0.0085f, i = 1.76f,
            periodDays = 60189.0f, moonsCount = 1, m0 = 259.91f, w = 273.25f, omega = 131.72f,
            tiltDeg = 28.3f, hasRings = true,
            rings = listOf(RingBand(1.5f, 1.7f, 0xFF888888, 0.25f)),
            accurateMoons = listOf(MoonData("Triton", -5.877f, 4.0f))
        ),
        PlanetData(
            key = "pluto", color = 0xFFDDDDDD, radius = 1.2f, a = 310f, e = 0.244f, i = 17.16f,
            periodDays = 90560.0f, moonsCount = 5, m0 = 14.5f, w = 113.8f, omega = 110.30f,
            tiltDeg = 122.5f,
            accurateMoons = listOf(
                MoonData("Charon", 6.387f, 2.0f),
                MoonData("Styx", 20.1f, 3.5f),
                MoonData("Nix", 24.8f, 5.0f),
                MoonData("Kerberos", 32.1f, 6.5f),
                MoonData("Hydra", 38.2f, 8.0f)
            )
        ),
        PlanetData(
            key = "haumea", color = 0xFFD4C4B4, radius = 1.0f, a = 340f, e = 0.196f, i = 28.19f,
            periodDays = 103774f, moonsCount = 2, m0 = 200f, w = 240f, omega = 122f,
            accurateMoons = listOf(MoonData("Hiʻiaka", 49.1f, 3.0f), MoonData("Namaka", 18.2f, 1.8f))
        ),
        PlanetData(
            key = "makemake", color = 0xFFCCAA99, radius = 0.9f, a = 380f, e = 0.161f, i = 29.0f,
            periodDays = 111666f, moonsCount = 1, m0 = 150f, w = 290f, omega = 79f,
            accurateMoons = listOf(MoonData("MK2", 12.4f, 2.0f))
        ),
        PlanetData(
            key = "eris", color = 0xFFEEEEEE, radius = 1.1f, a = 420f, e = 0.436f, i = 44.0f,
            periodDays = 203830f, moonsCount = 1, m0 = 100f, w = 150f, omega = 35f,
            accurateMoons = listOf(MoonData("Dysnomia", 15.7f, 2.5f))
        )
    )

    val halleyComet = PlanetData(
        key = "halley", color = 0xFF00FFFF, radius = 1.0f, a = 980f, e = 0.967f, i = 162.26f,
        periodDays = (75.3 * 365.25).toFloat(), moonsCount = 0, m0 = 65.64f, w = 111.33f, omega = 58.42f
    )

    val apophisAsteroid = PlanetData(
        key = "apophis", color = 0xFFFF3333, radius = 1.2f, a = 51f, e = 0.191f, i = 3.33f,
        periodDays = 323.6f, moonsCount = 0, m0 = 215.5f, w = 126.4f, omega = 204.4f
    )

    val nearbyStars = listOf(
        NearbyStar("proximaCentauri", 0xFFFF4444, 4f, 700f, 0.5f, -50f),
        NearbyStar("alphaCentauri", 0xFFFFDDAA, 6f, 750f, 0.55f, -30f),
        NearbyStar("barnardsStar", 0xFFFF6644, 3f, 900f, 2.1f, 120f),
        NearbyStar("sirius", 0xFFAADDFF, 8f, 1300f, 4.5f, -180f)
    )

    val constellations = listOf(
        ConstellationData("centaurus", 800f, 0.52f, -60f, listOf(
            floatArrayOf(0f, 100f, 0f), floatArrayOf(80f, 50f, 0f), floatArrayOf(150f, -50f, 0f),
            floatArrayOf(100f, -150f, 0f), floatArrayOf(0f, -100f, 0f), floatArrayOf(80f, 50f, 0f)
        )),
        ConstellationData("ursaMajor", 1400f, 1.5f, 350f, listOf(
            floatArrayOf(-150f, 100f, 0f), floatArrayOf(-50f, 80f, 20f), floatArrayOf(50f, 0f, 0f),
            floatArrayOf(100f, -50f, -20f), floatArrayOf(80f, -150f, -40f), floatArrayOf(200f, -180f, 0f),
            floatArrayOf(250f, -60f, 20f), floatArrayOf(100f, -50f, -20f)
        )),
        ConstellationData("orion", 1500f, 3.5f, 80f, listOf(
            floatArrayOf(-100f, 150f, 0f), floatArrayOf(100f, 120f, 0f), floatArrayOf(50f, 0f, 0f),
            floatArrayOf(0f, -20f, 0f), floatArrayOf(-50f, -40f, 0f), floatArrayOf(-120f, -180f, 0f),
            floatArrayOf(80f, -200f, 0f), floatArrayOf(50f, 0f, 0f)
        )),
        ConstellationData("canisMajor", 1350f, 4.4f, -220f, listOf(
            floatArrayOf(-50f, 80f, 0f), floatArrayOf(0f, 0f, 0f), floatArrayOf(80f, -50f, 0f),
            floatArrayOf(120f, -150f, 0f), floatArrayOf(40f, -100f, 0f), floatArrayOf(0f, 0f, 0f)
        )),
        ConstellationData("scorpius", 1100f, 2.2f, -100f, listOf(
            floatArrayOf(50f, 100f, 0f), floatArrayOf(0f, 50f, 0f), floatArrayOf(-30f, 0f, 0f),
            floatArrayOf(-40f, -50f, 0f), floatArrayOf(-20f, -100f, 0f), floatArrayOf(20f, -120f, 0f),
            floatArrayOf(60f, -90f, 0f)
        )),
        ConstellationData("sagittarius", 1200f, 2.6f, -150f, listOf(
            floatArrayOf(-50f, 50f, 0f), floatArrayOf(50f, 50f, 0f), floatArrayOf(80f, 0f, 0f),
            floatArrayOf(30f, -50f, 0f), floatArrayOf(-30f, -50f, 0f), floatArrayOf(-50f, 50f, 0f),
            floatArrayOf(30f, -50f, 0f), floatArrayOf(-100f, 0f, 0f), floatArrayOf(-50f, 50f, 0f)
        ))
    )

    fun getOrbitalPosition(
        a: Float,
        e: Float,
        iDeg: Float,
        omegaDeg: Float,
        wDeg: Float,
        mDeg: Float
    ): Vector3 {
        val i = Math.toRadians(iDeg.toDouble()).toFloat()
        val omega = Math.toRadians(omegaDeg.toDouble()).toFloat()
        val w = Math.toRadians(wDeg.toDouble()).toFloat()

        var mNorm = mDeg % 360f
        if (mNorm < 0f) mNorm += 360f
        val mRad = Math.toRadians(mNorm.toDouble()).toFloat()

        var eccentricAnomaly = mRad
        for (iter in 0 until 6) {
            val delta = (eccentricAnomaly - e * sin(eccentricAnomaly) - mRad) / (1f - e * cos(eccentricAnomaly))
            eccentricAnomaly -= delta
        }

        val v = 2f * atan2(
            sqrt(1f + e) * sin(eccentricAnomaly / 2f),
            sqrt(1f - e) * cos(eccentricAnomaly / 2f)
        )
        val r = a * (1f - e * e) / (1f + e * cos(v))

        val x = r * (cos(omega) * cos(w + v) - sin(omega) * sin(w + v) * cos(i))
        val y = r * (sin(omega) * cos(w + v) + cos(omega) * sin(w + v) * cos(i))
        val z = r * (sin(w + v) * sin(i))

        return Vector3(x, z, -y)
    }

    fun getSunGalacticPosition(angleRad: Float): Vector3 {
        val sunX = cos(angleRad) * SUN_GALACTIC_RADIUS
        val sunZ = -sin(angleRad) * SUN_GALACTIC_RADIUS
        val sunY = sin(angleRad * WAVES_PER_ORBIT) * WAVE_AMPLITUDE
        return Vector3(sunX, sunY, sunZ)
    }

    fun getSunForwardVector(angleRad: Float): Vector3 {
        val dirX = -sin(angleRad) * SUN_GALACTIC_RADIUS
        val dirZ = -cos(angleRad) * SUN_GALACTIC_RADIUS
        val dirY = WAVES_PER_ORBIT * WAVE_AMPLITUDE * cos(angleRad * WAVES_PER_ORBIT)
        return Vector3(dirX, dirY, dirZ).normalized()
    }

    fun getSystemTransform(sunPos: Vector3, forwardDir: Vector3): Matrix4 {
        val alignMat = Matrix4.alignToDirection(forwardDir)
        val tiltMat = Matrix4.rotationX((PI / 3).toFloat())
        val transMat = Matrix4.translation(sunPos.x, sunPos.y, sunPos.z)
        return transMat.multiply(alignMat.multiply(tiltMat))
    }
}
