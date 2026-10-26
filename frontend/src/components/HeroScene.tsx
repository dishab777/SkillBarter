import { Canvas, useFrame } from "@react-three/fiber";
import { Float, MeshDistortMaterial, OrbitControls, Sparkles, Torus } from "@react-three/drei";
import { Suspense, useRef } from "react";
import type { Group } from "three";

function ExchangeSculpture() {
  const group = useRef<Group>(null);
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  useFrame((_, delta) => {
    if (group.current && !reduceMotion) group.current.rotation.y += delta * 0.12;
  });

  return (
    <group ref={group} rotation={[0.18, 0.25, -0.16]}>
      <Float speed={reduceMotion ? 0 : 1.1} rotationIntensity={reduceMotion ? 0 : 0.18} floatIntensity={reduceMotion ? 0 : 0.3}>
        <mesh position={[-0.67, 0.15, 0]}>
          <icosahedronGeometry args={[0.94, 2]} />
          <MeshDistortMaterial color="#cbdc83" roughness={0.3} metalness={0.15} distort={0.2} speed={reduceMotion ? 0 : 1.2} />
        </mesh>
        <mesh position={[0.64, -0.1, 0.12]} rotation={[0.3, 0.25, -0.25]}>
          <octahedronGeometry args={[0.93, 1]} />
          <meshStandardMaterial color="#e49b76" roughness={0.34} metalness={0.12} />
        </mesh>
        <Torus args={[1.14, 0.035, 12, 100]} rotation={[0.55, 0.15, 0.72]}>
          <meshStandardMaterial color="#fff6de" metalness={0.6} roughness={0.24} />
        </Torus>
        <Torus args={[1.12, 0.027, 12, 100]} rotation={[-0.58, 0.2, -0.74]}>
          <meshStandardMaterial color="#5c9282" metalness={0.55} roughness={0.25} />
        </Torus>
      </Float>
      <Sparkles count={28} scale={4.3} size={2.5} speed={reduceMotion ? 0 : 0.18} color="#d6e18f" />
    </group>
  );
}

export default function HeroScene() {
  if (window.matchMedia("(max-width: 760px)").matches) {
    return <div className="hero-mobile-sculpture" aria-hidden="true"><span /><span /></div>;
  }

  return (
    <div className="hero-scene" aria-label="A pair of interlocking shapes representing a skill exchange" role="img">
      <Canvas camera={{ position: [0, 0, 5.2], fov: 40 }} dpr={[1, 1.5]} gl={{ alpha: true, antialias: true }}>
        <ambientLight intensity={1.45} />
        <directionalLight position={[3, 4, 4]} intensity={2.2} color="#fff4dc" />
        <directionalLight position={[-4, -2, 3]} intensity={1.2} color="#8ec4a9" />
        <Suspense fallback={null}>
          <ExchangeSculpture />
        </Suspense>
        <OrbitControls enableZoom={false} enablePan={false} enableRotate={false} />
      </Canvas>
    </div>
  );
}
