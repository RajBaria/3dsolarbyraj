package com.example.solarsystem.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.solarsystem.graphics.Matrix4
import com.example.solarsystem.graphics.Vector3
import com.example.solarsystem.model.AstronomicalData
import com.example.solarsystem.model.Localization
import com.example.solarsystem.model.PlanetData
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class ProjectedLabel(
    val key: String,
    val text: String,
    val x: Float,
    val y: Float,
    val color: Color,
    val isHeader: Boolean = false,
    val isMoon: Boolean = false,
    val onClickTarget: String? = null
)

class GalaxyStar(
    val radius: Float,
    val angle: Float,
    val y: Float,
    val color: Color,
    val size: Float
)

class AsteroidParticle(
    val dist: Float,
    val angle0: Float,
    val speed: Float,
    val yOffset: Float,
    val size: Float
)

class OortParticle(
    val r: Float,
    val theta: Float,
    val phi: Float
)

@Composable
fun SolarCanvas(
    state: SimulationState,
    onRotate: (Float, Float) -> Unit,
    onZoom: (Float) -> Unit,
    onSelectTarget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strings = remember(state.language) { Localization.get(state.language) }

    // Precompute procedural particles for Galaxy, Asteroids, Oort cloud
    val galaxyStars = remember {
        val list = ArrayList<GalaxyStar>(3500)
        val rng = Random(42)
        val arms = 5
        val maxRadius = 8000f
        val colorInside = Color(0xFFFFEEBB)
        val colorOutside = Color(0xFF1166FF)
        val colorHalo = Color(0xFFFFCC88)

        for (i in 0 until 3500) {
            val isHalo = rng.nextFloat() < 0.05f
            if (isHalo) {
                val r = Math.pow(rng.nextDouble(), 2.5).toFloat() * (maxRadius + 1000f)
                val theta = rng.nextFloat() * 2f * PI.toFloat()
                val phi = kotlin.math.acos(2f * rng.nextFloat() - 1f)
                list.add(
                    GalaxyStar(
                        radius = r,
                        angle = theta,
                        y = r * cos(phi) * 0.3f,
                        color = colorHalo.copy(alpha = 0.4f),
                        size = 1.5f + rng.nextFloat() * 2.0f
                    )
                )
            } else {
                val rNorm = rng.nextFloat()
                val radius = Math.pow(rNorm.toDouble(), 1.8).toFloat() * maxRadius
                val spinAngle = radius * 5f / maxRadius
                val branchAngle = ((i % arms).toFloat() / arms) * 2f * PI.toFloat()
                val scatter = Math.pow(rng.nextDouble(), 2.0).toFloat() * (if (rng.nextBoolean()) 1f else -1f)
                val finalAngle = branchAngle + spinAngle + (scatter * 0.3f)

                val baseWavyY = sin(finalAngle * 6f) * (radius / maxRadius * 180f * 1.5f)
                val py = baseWavyY + (Math.pow(rng.nextDouble(), 3.0).toFloat() * (if (rng.nextBoolean()) 1f else -1f)) * 200f * (1f - radius / maxRadius)

                val t = (radius / maxRadius).coerceIn(0f, 1f)
                val starColor = Color(
                    red = colorInside.red * (1f - t) + colorOutside.red * t,
                    green = colorInside.green * (1f - t) + colorOutside.green * t,
                    blue = colorInside.blue * (1f - t) + colorOutside.blue * t,
                    alpha = 0.65f
                )
                list.add(
                    GalaxyStar(
                        radius = radius,
                        angle = finalAngle,
                        y = py,
                        color = starColor,
                        size = 1.2f + rng.nextFloat() * 2.5f
                    )
                )
            }
        }
        list
    }

    val asteroidParticles = remember {
        val list = ArrayList<AsteroidParticle>(600)
        val rng = Random(1337)
        for (i in 0 until 600) {
            val dist = 85f + rng.nextFloat() * 25f
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val speed = (2f * PI.toFloat()) / (dist * dist * 0.1f)
            val yOffset = (rng.nextFloat() - 0.5f) * 8f
            val size = 0.8f + rng.nextFloat() * 1.5f
            list.add(AsteroidParticle(dist, angle, speed, yOffset, size))
        }
        list
    }

    val oortParticles = remember {
        val list = ArrayList<OortParticle>(500)
        val rng = Random(2026)
        for (i in 0 until 500) {
            val r = 800f + rng.nextFloat() * 700f
            val theta = rng.nextFloat() * 2f * PI.toFloat()
            val phi = kotlin.math.acos(rng.nextFloat() * 2f - 1f)
            list.add(OortParticle(r, theta, phi))
        }
        list
    }

    var projectedLabels by remember { mutableStateOf<List<ProjectedLabel>>(emptyList()) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f) {
                            onZoom(1f / zoom)
                        }
                        if (pan.x != 0f || pan.y != 0f) {
                            onRotate(pan.x * 0.005f, -pan.y * 0.005f)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            // Compute simulation parameters
            val elapsed = state.elapsedDays.toFloat()
            // Forward galactic motion (0.015 rad/yr = 0.015 / 365.25 rad/day) so Sun visibly moves through the galaxy with time:
            val galBaseSpeed = (elapsed * (0.015f / 365.25f)) + (state.elapsedDays * (2.0 * PI / AstronomicalData.GALACTIC_ORBIT_DAYS)).toFloat()
            val galAngleOffsetRad = Math.toRadians(state.galacticAngleDeg.toDouble()).toFloat()
            val systemGalAngle = galBaseSpeed + galAngleOffsetRad

            // Sun galactic position & system transform
            val sunPos = AstronomicalData.getSunGalacticPosition(systemGalAngle)
            val sunForward = AstronomicalData.getSunForwardVector(systemGalAngle)
            val systemTransform = AstronomicalData.getSystemTransform(sunPos, sunForward)

            // Calculate planet positions in system space & world space
            val planetWorldPositions = mutableMapOf<String, Vector3>()
            val planetSystemPositions = mutableMapOf<String, Vector3>()

            AstronomicalData.planets.forEach { p ->
                val mDeg = p.m0 + (elapsed * (360f / p.periodDays))
                val localPos = AstronomicalData.getOrbitalPosition(p.a, p.e, p.i, p.omega, p.w, mDeg)
                planetSystemPositions[p.key] = localPos
                val worldPos = systemTransform.transform(localPos)
                planetWorldPositions[p.key] = worldPos
            }

            // Apophis world position
            val apoMDeg = AstronomicalData.apophisAsteroid.m0 + (elapsed * (360f / AstronomicalData.apophisAsteroid.periodDays))
            val apoLocalPos = AstronomicalData.getOrbitalPosition(
                AstronomicalData.apophisAsteroid.a, AstronomicalData.apophisAsteroid.e,
                AstronomicalData.apophisAsteroid.i, AstronomicalData.apophisAsteroid.omega,
                AstronomicalData.apophisAsteroid.w, apoMDeg
            )
            val apoWorldPos = systemTransform.transform(apoLocalPos)
            planetWorldPositions["apophis"] = apoWorldPos

            // Halley comet world position
            val halleyMDeg = AstronomicalData.halleyComet.m0 + (elapsed * (360f / AstronomicalData.halleyComet.periodDays))
            val halleyLocalPos = AstronomicalData.getOrbitalPosition(
                AstronomicalData.halleyComet.a, AstronomicalData.halleyComet.e,
                AstronomicalData.halleyComet.i, AstronomicalData.halleyComet.omega,
                AstronomicalData.halleyComet.w, halleyMDeg
            )
            val halleyWorldPos = systemTransform.transform(halleyLocalPos)
            planetWorldPositions["halley"] = halleyWorldPos

            // Determine camera target based on focusTarget
            val cameraTarget = when (state.focusTarget) {
                "sun" -> sunPos
                "apophis" -> apoWorldPos
                "halley" -> halleyWorldPos
                "free" -> Vector3(0f, 0f, 0f)
                else -> planetWorldPositions[state.focusTarget] ?: sunPos
            }

            // Compute camera eye position
            val dist = state.cameraDistance
            val yaw = state.cameraYaw
            val pitch = state.cameraPitch
            val eye = Vector3(
                cameraTarget.x + dist * cos(pitch) * sin(yaw),
                cameraTarget.y + dist * sin(pitch),
                cameraTarget.z + dist * cos(pitch) * cos(yaw)
            )

            val viewMat = Matrix4.lookAt(eye, cameraTarget, Vector3(0f, 1f, 0f))
            val aspect = width / height
            val projMat = Matrix4.perspective(60f, aspect, 1f, 30000f)
            val viewProj = projMat.multiply(viewMat)

            fun projectToScreen(worldPos: Vector3): Vector3? {
                // View space
                val vx = viewMat.m[0] * worldPos.x + viewMat.m[1] * worldPos.y + viewMat.m[2] * worldPos.z + viewMat.m[3]
                val vy = viewMat.m[4] * worldPos.x + viewMat.m[5] * worldPos.y + viewMat.m[6] * worldPos.z + viewMat.m[7]
                val vz = viewMat.m[8] * worldPos.x + viewMat.m[9] * worldPos.y + viewMat.m[10] * worldPos.z + viewMat.m[11]

                // Point is behind the camera
                if (vz >= -1f) return null

                // Project
                val px = viewProj.transform(worldPos)
                val screenX = (px.x * 0.5f + 0.5f) * width
                val screenY = (-px.y * 0.5f + 0.5f) * height

                return Vector3(screenX, screenY, -vz)
            }

            // 1. Draw Deep Space background
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0D1429), Color(0xFF060914), Color(0xFF000000)),
                    center = Offset(width / 2f, height / 2f),
                    radius = max(width, height)
                )
            )

            val newLabels = mutableListOf<ProjectedLabel>()

            // 2. Draw Galaxy (Sagittarius A* and spiral stars)
            if (state.showGalaxy) {
                // Sagittarius A* core
                val sagAPos = Vector3(0f, 0f, 0f)
                val sagAScreen = projectToScreen(sagAPos)
                if (sagAScreen != null) {
                    val radius = (1200f / sagAScreen.z).coerceIn(4f, 70f)
                    // Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFEEAA).copy(alpha = 0.8f), Color(0xFFFF8800).copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(sagAScreen.x, sagAScreen.y),
                            radius = radius * 3f
                        ),
                        radius = radius * 3f,
                        center = Offset(sagAScreen.x, sagAScreen.y)
                    )
                    drawCircle(
                        color = Color(0xFFFF8800),
                        radius = radius,
                        center = Offset(sagAScreen.x, sagAScreen.y)
                    )

                    newLabels.add(
                        ProjectedLabel(
                            key = "galCenter",
                            text = strings.names["galCenter"] ?: "Sagittarius A*",
                            x = sagAScreen.x,
                            y = sagAScreen.y - radius - 15f,
                            color = Color(0xFFFFAA00),
                            isHeader = true
                        )
                    )
                }

                // Spiral stars
                galaxyStars.forEach { star ->
                    val angle = star.angle + galBaseSpeed
                    val wx = cos(angle) * star.radius
                    val wz = -sin(angle) * star.radius
                    val wy = star.y
                    val sProj = projectToScreen(Vector3(wx, wy, wz))
                    if (sProj != null && sProj.x in -50f..width + 50f && sProj.y in -50f..height + 50f) {
                        val pSize = (star.size * 300f / sProj.z).coerceIn(0.8f, 3.5f)
                        drawCircle(
                            color = star.color,
                            radius = pSize,
                            center = Offset(sProj.x, sProj.y)
                        )
                    }
                }
            }

            // 3. Draw Galactic Orbit Path
            if (state.showGalOrbit && (state.pathVisibility["sun"] ?: true)) {
                val path = Path()
                var started = false
                for (step in 0..180) {
                    val theta = (step / 180f) * 2f * PI.toFloat()
                    val pos = AstronomicalData.getSunGalacticPosition(theta)
                    val s = projectToScreen(pos)
                    if (s != null) {
                        if (!started) {
                            path.moveTo(s.x, s.y)
                            started = true
                        } else {
                            path.lineTo(s.x, s.y)
                        }
                    }
                }
                if (started) {
                    drawPath(
                        path = path,
                        color = Color(0xFF00AAFF).copy(alpha = 0.5f),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // 4. Draw Helical Trails
            if (state.showHelicalPaths) {
                AstronomicalData.planets.forEach { p ->
                    if (state.pathVisibility[p.key] != false) {
                        val trailYears = max(1.5f, (p.periodDays / 365.25f) * 1.2f)
                        val segments = 40
                        val trailPath = Path()
                        var trailStarted = false

                        for (seg in 0..segments) {
                            val progress = 1f - (seg.toFloat() / segments)
                            val pastDays = progress * (trailYears * 365.25f)
                            val pastSimDay = elapsed - pastDays

                            val pastMDeg = p.m0 + (pastSimDay * (360f / p.periodDays))
                            val localP = AstronomicalData.getOrbitalPosition(p.a, p.e, p.i, p.omega, p.w, pastMDeg)

                            val pastGalAngle = systemGalAngle - (pastDays / 365.25f) * 0.015f
                            val pastSunPos = AstronomicalData.getSunGalacticPosition(pastGalAngle)
                            val pastForward = AstronomicalData.getSunForwardVector(pastGalAngle)
                            val pastTrans = AstronomicalData.getSystemTransform(pastSunPos, pastForward)

                            val worldP = pastTrans.transform(localP)
                            val screenP = projectToScreen(worldP)
                            if (screenP != null) {
                                if (!trailStarted) {
                                    trailPath.moveTo(screenP.x, screenP.y)
                                    trailStarted = true
                                } else {
                                    trailPath.lineTo(screenP.x, screenP.y)
                                }
                            }
                        }
                        if (trailStarted) {
                            drawPath(
                                path = trailPath,
                                color = Color(p.color).copy(alpha = 0.7f),
                                style = Stroke(width = 1.8f)
                            )
                        }
                    }
                }

                // Apophis Helical trail
                if (state.showAsteroidBelt && state.pathVisibility["apophis"] != false) {
                    val trailPath = Path()
                    var trailStarted = false
                    for (seg in 0..30) {
                        val progress = 1f - (seg.toFloat() / 30)
                        val pastDays = progress * (1.5f * 365.25f)
                        val pastSimDay = elapsed - pastDays
                        val pastMDeg = AstronomicalData.apophisAsteroid.m0 + (pastSimDay * (360f / AstronomicalData.apophisAsteroid.periodDays))
                        val localP = AstronomicalData.getOrbitalPosition(
                            AstronomicalData.apophisAsteroid.a, AstronomicalData.apophisAsteroid.e,
                            AstronomicalData.apophisAsteroid.i, AstronomicalData.apophisAsteroid.omega,
                            AstronomicalData.apophisAsteroid.w, pastMDeg
                        )
                        val pastGalAngle = systemGalAngle - (pastDays / 365.25f) * 0.015f
                        val pastSunPos = AstronomicalData.getSunGalacticPosition(pastGalAngle)
                        val pastForward = AstronomicalData.getSunForwardVector(pastGalAngle)
                        val pastTrans = AstronomicalData.getSystemTransform(pastSunPos, pastForward)
                        val screenP = projectToScreen(pastTrans.transform(localP))
                        if (screenP != null) {
                            if (!trailStarted) {
                                trailPath.moveTo(screenP.x, screenP.y)
                                trailStarted = true
                            } else {
                                trailPath.lineTo(screenP.x, screenP.y)
                            }
                        }
                    }
                    if (trailStarted) {
                        drawPath(
                            path = trailPath,
                            color = Color(0xFFFF3333).copy(alpha = 0.8f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            // 5. Draw Solar Planetary Orbit Ellipses
            if (state.showSolarPaths) {
                AstronomicalData.planets.forEach { p ->
                    if (state.pathVisibility[p.key] != false) {
                        val orbitPath = Path()
                        var orbitStarted = false
                        val segCount = 72
                        for (s in 0..segCount) {
                            val m = (s.toFloat() / segCount) * 360f
                            val lPos = AstronomicalData.getOrbitalPosition(p.a, p.e, p.i, p.omega, p.w, m)
                            val wPos = systemTransform.transform(lPos)
                            val sp = projectToScreen(wPos)
                            if (sp != null) {
                                if (!orbitStarted) {
                                    orbitPath.moveTo(sp.x, sp.y)
                                    orbitStarted = true
                                } else {
                                    orbitPath.lineTo(sp.x, sp.y)
                                }
                            }
                        }
                        if (orbitStarted) {
                            drawPath(
                                path = orbitPath,
                                color = Color(p.color).copy(alpha = 0.45f),
                                style = Stroke(width = 1.2f)
                            )
                        }

                        // Draw each moon's orbit path around this planet
                        if (p.accurateMoons.isNotEmpty()) {
                            val sysPos = planetSystemPositions[p.key] ?: Vector3()
                            p.accurateMoons.forEach { moon ->
                                val mDist = p.radius + moon.dist
                                val moonOrbitPath = Path()
                                var moonStarted = false
                                val mSegCount = 36
                                for (ms in 0..mSegCount) {
                                    val mAng = (ms.toFloat() / mSegCount) * 2f * PI.toFloat()
                                    val mLocal = Vector3(cos(mAng) * mDist, 0f, -sin(mAng) * mDist)
                                    val mWorld = systemTransform.transform(sysPos + mLocal)
                                    val mScr = projectToScreen(mWorld)
                                    if (mScr != null) {
                                        if (!moonStarted) {
                                            moonOrbitPath.moveTo(mScr.x, mScr.y)
                                            moonStarted = true
                                        } else {
                                            moonOrbitPath.lineTo(mScr.x, mScr.y)
                                        }
                                    }
                                }
                                if (moonStarted) {
                                    drawPath(
                                        path = moonOrbitPath,
                                        color = Color(0xFFD8B4E2).copy(alpha = 0.45f),
                                        style = Stroke(width = 1.0f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Apophis orbit ellipse
                if (state.showAsteroidBelt && state.pathVisibility["apophis"] != false) {
                    val apoPath = Path()
                    var apoStarted = false
                    for (s in 0..60) {
                        val m = (s.toFloat() / 60) * 360f
                        val lPos = AstronomicalData.getOrbitalPosition(
                            AstronomicalData.apophisAsteroid.a, AstronomicalData.apophisAsteroid.e,
                            AstronomicalData.apophisAsteroid.i, AstronomicalData.apophisAsteroid.omega,
                            AstronomicalData.apophisAsteroid.w, m
                        )
                        val sp = projectToScreen(systemTransform.transform(lPos))
                        if (sp != null) {
                            if (!apoStarted) {
                                apoPath.moveTo(sp.x, sp.y)
                                apoStarted = true
                            } else {
                                apoPath.lineTo(sp.x, sp.y)
                            }
                        }
                    }
                    if (apoStarted) {
                        drawPath(
                            path = apoPath,
                            color = Color(0xFFFF2222).copy(alpha = 0.7f),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }

                // Halley Comet orbit ellipse
                if (state.showCometOort) {
                    val halleyPath = Path()
                    var halleyStarted = false
                    for (s in 0..120) {
                        val m = (s.toFloat() / 120) * 360f
                        val lPos = AstronomicalData.getOrbitalPosition(
                            AstronomicalData.halleyComet.a, AstronomicalData.halleyComet.e,
                            AstronomicalData.halleyComet.i, AstronomicalData.halleyComet.omega,
                            AstronomicalData.halleyComet.w, m
                        )
                        val sp = projectToScreen(systemTransform.transform(lPos))
                        if (sp != null) {
                            if (!halleyStarted) {
                                halleyPath.moveTo(sp.x, sp.y)
                                halleyStarted = true
                            } else {
                                halleyPath.lineTo(sp.x, sp.y)
                            }
                        }
                    }
                    if (halleyStarted) {
                        drawPath(
                            path = halleyPath,
                            color = Color(0xFF00FFFF).copy(alpha = 0.5f),
                            style = Stroke(width = 1.2f)
                        )
                    }
                }
            }

            // 6. Draw Asteroid Belt (Always rendered in simulation; showAsteroidBelt toggles names/labels)
            asteroidParticles.forEach { ast ->
                val angle = ast.angle0 + elapsed * ast.speed
                val lx = cos(angle) * ast.dist
                val lz = -sin(angle) * ast.dist
                val ly = ast.yOffset
                val sp = projectToScreen(systemTransform.transform(Vector3(lx, ly, lz)))
                if (sp != null && sp.x in 0f..width && sp.y in 0f..height) {
                    val rad = (ast.size * 180f / sp.z).coerceIn(0.8f, 3.0f)
                    drawCircle(
                        color = Color(0xFFCCCCCC).copy(alpha = 0.8f),
                        radius = rad,
                        center = Offset(sp.x, sp.y)
                    )
                }
            }

            // Apophis (NEO)
            val apoScr = projectToScreen(apoWorldPos)
            if (apoScr != null) {
                val rad = (AstronomicalData.apophisAsteroid.radius * 350f / apoScr.z).coerceIn(3f, 18f)
                // Red hazard alert pulse glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF0000).copy(alpha = 0.7f), Color.Transparent),
                        center = Offset(apoScr.x, apoScr.y),
                        radius = rad * 3f
                    ),
                    radius = rad * 3f,
                    center = Offset(apoScr.x, apoScr.y)
                )
                drawCircle(
                    color = Color(0xFFFF3333),
                    radius = rad,
                    center = Offset(apoScr.x, apoScr.y)
                )

                if (state.showAsteroidBelt) {
                    newLabels.add(
                        ProjectedLabel(
                            key = "apophis",
                            text = strings.names["apophis"] ?: "Apophis (NEO)",
                            x = apoScr.x,
                            y = apoScr.y - rad - 12f,
                            color = Color(0xFFFF3333),
                            isHeader = true,
                            onClickTarget = "apophis"
                        )
                    )
                }
            }

            // Asteroid Belt Name Label
            if (state.showAsteroidBelt) {
                val beltPos = systemTransform.transform(Vector3(95f, 3f, 0f))
                val beltScr = projectToScreen(beltPos)
                if (beltScr != null && beltScr.x in 0f..width && beltScr.y in 0f..height) {
                    newLabels.add(
                        ProjectedLabel(
                            key = "asteroidBelt",
                            text = strings.names["asteroidBelt"] ?: strings.asteroidBelt.trim().trimEnd(':'),
                            x = beltScr.x,
                            y = beltScr.y - 12f,
                            color = Color(0xFFCCCCCC),
                            isHeader = true,
                            onClickTarget = "ceres"
                        )
                    )
                }
            }

            // 7. Draw Comet & Oort Cloud
            if (state.showCometOort) {
                // Oort cloud
                oortParticles.forEach { o ->
                    val lx = o.r * sin(o.phi) * cos(o.theta)
                    val ly = o.r * cos(o.phi)
                    val lz = -o.r * sin(o.phi) * sin(o.theta)
                    val sp = projectToScreen(systemTransform.transform(Vector3(lx, ly, lz)))
                    if (sp != null && sp.x in 0f..width && sp.y in 0f..height) {
                        drawCircle(
                            color = Color(0xFF88AAFF).copy(alpha = 0.4f),
                            radius = 1.2f,
                            center = Offset(sp.x, sp.y)
                        )
                    }
                }

                // Halley Comet with tails
                val hScr = projectToScreen(halleyWorldPos)
                if (hScr != null) {
                    val distToSun = (halleyLocalPos).length()
                    val intensity = max(0f, 1f - (distToSun / 250f))

                    // Tail points away from the Sun (sun is at systemTransform.origin)
                    val sunScr = projectToScreen(sunPos)
                    if (sunScr != null) {
                        val tailDir = Vector3(hScr.x - sunScr.x, hScr.y - sunScr.y, 0f).normalized()

                        // Ion tail (Cyan / Blue)
                        val ionLen = 40f + intensity * 80f
                        drawLine(
                            color = Color(0xFF4488FF).copy(alpha = 0.7f * intensity + 0.2f),
                            start = Offset(hScr.x, hScr.y),
                            end = Offset(hScr.x + tailDir.x * ionLen, hScr.y + tailDir.y * ionLen),
                            strokeWidth = 3.5f
                        )

                        // Dust tail (warm glow)
                        val dustLen = 30f + intensity * 60f
                        val dustDir = Vector3(tailDir.x * cos(0.25f) - tailDir.y * sin(0.25f), tailDir.x * sin(0.25f) + tailDir.y * cos(0.25f), 0f)
                        drawLine(
                            color = Color(0xFFFFDDAA).copy(alpha = 0.5f * intensity + 0.15f),
                            start = Offset(hScr.x, hScr.y),
                            end = Offset(hScr.x + dustDir.x * dustLen, hScr.y + dustDir.y * dustLen),
                            strokeWidth = 5f
                        )
                    }

                    // Coma & nucleus
                    val comaRadius = (4f + intensity * 10f).coerceIn(4f, 30f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFCCFFFF), Color(0xFF00FFFF).copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(hScr.x, hScr.y),
                            radius = comaRadius * 2f
                        ),
                        radius = comaRadius * 2f,
                        center = Offset(hScr.x, hScr.y)
                    )
                    drawCircle(color = Color.White, radius = 2.5f, center = Offset(hScr.x, hScr.y))

                    newLabels.add(
                        ProjectedLabel(
                            key = "halley",
                            text = strings.names["halley"] ?: "Halley's Comet",
                            x = hScr.x,
                            y = hScr.y - comaRadius - 12f,
                            color = Color(0xFF00FFFF),
                            onClickTarget = "halley"
                        )
                    )
                }
            }

            // 8. Draw Nearby Stars & Constellations
            if (state.showStarLabels) {
                AstronomicalData.nearbyStars.forEach { star ->
                    val lx = cos(star.angle) * star.dist
                    val lz = -sin(star.angle) * star.dist
                    val ly = star.height
                    val sp = projectToScreen(systemTransform.transform(Vector3(lx, ly, lz)))
                    if (sp != null) {
                        val rad = (star.radius * 250f / sp.z).coerceIn(3f, 15f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(star.color), Color(star.color).copy(alpha = 0.3f), Color.Transparent),
                                center = Offset(sp.x, sp.y),
                                radius = rad * 2.5f
                            ),
                            radius = rad * 2.5f,
                            center = Offset(sp.x, sp.y)
                        )
                        drawCircle(color = Color(star.color), radius = rad, center = Offset(sp.x, sp.y))

                        newLabels.add(
                            ProjectedLabel(
                                key = star.key,
                                text = strings.names[star.key] ?: star.key,
                                x = sp.x,
                                y = sp.y - rad - 10f,
                                color = Color(0xFFFFCCCC)
                            )
                        )
                    }
                }
            }

            if (state.showConstellationLines) {
                AstronomicalData.constellations.forEach { c ->
                    val cx = cos(c.angle) * c.dist
                    val cz = -sin(c.angle) * c.dist
                    val cy = c.height

                    val screenPoints = c.pointsOffsets.mapNotNull { p ->
                        val local = Vector3(cx + p[0], cy + p[1], cz + p[2])
                        projectToScreen(systemTransform.transform(local))
                    }

                    if (screenPoints.size >= 2) {
                        for (i in 0 until screenPoints.size - 1) {
                            val p1 = screenPoints[i]
                            val p2 = screenPoints[i + 1]
                            drawLine(
                                color = Color(0xFFFFFF88).copy(alpha = 0.55f),
                                start = Offset(p1.x, p1.y),
                                end = Offset(p2.x, p2.y),
                                strokeWidth = 1.5f
                            )
                        }
                        screenPoints.forEach { sp ->
                            drawCircle(color = Color.White, radius = 2.5f, center = Offset(sp.x, sp.y))
                        }

                        val mid = screenPoints[screenPoints.size / 2]
                        if (state.showStarLabels) {
                            newLabels.add(
                                ProjectedLabel(
                                    key = c.key,
                                    text = strings.names[c.key] ?: c.key,
                                    x = mid.x,
                                    y = mid.y - 12f,
                                    color = Color(0xFFCCCCFF)
                                )
                            )
                        }
                    }
                }
            }

            // 9. Draw the Sun
            val sunScr = projectToScreen(sunPos)
            if (sunScr != null) {
                val sunRadius = (12f * 350f / sunScr.z).coerceIn(8f, 80f)

                // Sun Corona Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF099), Color(0xFFFFAA00).copy(alpha = 0.5f), Color(0xFFFF5500).copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(sunScr.x, sunScr.y),
                        radius = sunRadius * 3.5f
                    ),
                    radius = sunRadius * 3.5f,
                    center = Offset(sunScr.x, sunScr.y)
                )

                // Sun Sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFEA00), Color(0xFFFF8800)),
                        center = Offset(sunScr.x - sunRadius * 0.2f, sunScr.y - sunRadius * 0.2f),
                        radius = sunRadius
                    ),
                    radius = sunRadius,
                    center = Offset(sunScr.x, sunScr.y)
                )

                newLabels.add(
                    ProjectedLabel(
                        key = "sun",
                        text = strings.names["sun"] ?: "Sun",
                        x = sunScr.x,
                        y = sunScr.y - sunRadius - 16f,
                        color = Color(0xFFFFD700),
                        isHeader = true,
                        onClickTarget = "sun"
                    )
                )
            }

            // 10. Draw Planets and Moons (sorted by depth for proper rendering)
            val sortedPlanets = AstronomicalData.planets.mapNotNull { p ->
                val wPos = planetWorldPositions[p.key] ?: return@mapNotNull null
                val sPos = projectToScreen(wPos) ?: return@mapNotNull null
                Triple(p, wPos, sPos)
            }.sortedByDescending { it.third.z }

            sortedPlanets.forEach { (p, worldPos, screenPos) ->
                val pRadius = (p.radius * 300f / screenPos.z).coerceIn(2.5f, 50f)

                // Shaded 3D sphere gradient (illuminated from Sun)
                val toSun = (sunPos - worldPos).normalized()
                val lightOffsetX = toSun.x * pRadius * 0.4f
                val lightOffsetY = -toSun.y * pRadius * 0.4f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.8f),
                            Color(p.color),
                            Color(p.color).copy(alpha = 0.4f),
                            Color(0xFF101015)
                        ),
                        center = Offset(screenPos.x + lightOffsetX, screenPos.y + lightOffsetY),
                        radius = pRadius
                    ),
                    radius = pRadius,
                    center = Offset(screenPos.x, screenPos.y)
                )

                // Planetary Rings (Saturn, Jupiter, Uranus, Neptune)
                if (p.hasRings && p.rings.isNotEmpty()) {
                    p.rings.forEach { r ->
                        val rInner = pRadius * r.innerFactor
                        val rOuter = pRadius * r.outerFactor
                        val ringWidth = (rOuter - rInner).coerceAtLeast(1.5f)
                        drawOval(
                            color = Color(r.color).copy(alpha = r.alpha),
                            topLeft = Offset(screenPos.x - rOuter, screenPos.y - rOuter * 0.45f),
                            size = androidx.compose.ui.geometry.Size(rOuter * 2f, rOuter * 0.9f),
                            style = Stroke(width = ringWidth)
                        )
                    }
                }

                // Moons
                if (p.accurateMoons.isNotEmpty()) {
                    p.accurateMoons.forEachIndexed { mIdx, moon ->
                        val moonAngle = (elapsed * (2f * PI.toFloat() / moon.periodDays)) + (mIdx * 1.5f)
                        val mDist = p.radius + moon.dist
                        val mLocal = Vector3(cos(moonAngle) * mDist, 0f, -sin(moonAngle) * mDist)

                        val sysPos = planetSystemPositions[p.key] ?: Vector3()
                        val moonWorld = systemTransform.transform(sysPos + mLocal)
                        val mScr = projectToScreen(moonWorld)
                        if (mScr != null) {
                            val mRad = (pRadius * 0.2f).coerceIn(1.2f, 4f)
                            drawCircle(color = Color(0xFFDDDDDD), radius = mRad, center = Offset(mScr.x, mScr.y))

                            if (state.showMoonLabels) {
                                val moonName = strings.moonNames[p.key]?.getOrNull(mIdx) ?: moon.name
                                newLabels.add(
                                    ProjectedLabel(
                                        key = "${p.key}_$mIdx",
                                        text = moonName,
                                        x = mScr.x,
                                        y = mScr.y - mRad - 8f,
                                        color = Color(0xFFD8B4E2),
                                        isMoon = true
                                    )
                                )
                            }
                        }
                    }
                }

                // Planet Label
                if (state.showPlanetLabels) {
                    newLabels.add(
                        ProjectedLabel(
                            key = p.key,
                            text = strings.names[p.key] ?: p.key,
                            x = screenPos.x,
                            y = screenPos.y - pRadius - 12f,
                            color = Color.White,
                            onClickTarget = p.key
                        )
                    )
                }
            }

            projectedLabels = newLabels
        }

        // Render Floating Screen Labels
        projectedLabels.forEach { label ->
            if (label.x in 20f..density.run { 2000.sp.toPx() } && label.y > 60f) {
                Box(
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                val px = (label.x - placeable.width / 2).toInt()
                                val py = (label.y - placeable.height / 2).toInt()
                                placeable.placeRelative(IntOffset(px, py))
                            }
                        }
                        .pointerInput(label.onClickTarget) {
                            if (label.onClickTarget != null) {
                                detectTapGestures {
                                    onSelectTarget(label.onClickTarget)
                                }
                            }
                        }
                ) {
                    Text(
                        text = label.text,
                        color = label.color,
                        fontSize = when {
                            label.isHeader -> 13.sp
                            label.isMoon -> 9.sp
                            else -> 11.sp
                        },
                        fontWeight = if (label.isHeader) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}
