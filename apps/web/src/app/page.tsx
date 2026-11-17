'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';

export default function WelcomeSplashPage() {
  const router = useRouter();
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const touchStartXRef = useRef<number | null>(null);

  const [animationComplete, setAnimationComplete] = useState(false);
  const [hasNavigated, setHasNavigated] = useState(false);

  // Returning authenticated users who completed onboarding go directly to dashboard
  useEffect(() => {
    fetch('/api/profile')
      .then((r) => (r.ok ? r.json() : null))
      .then((json) => {
        if (json?.user?.onboardingCompleted) {
          router.replace('/dashboard');
        }
      })
      .catch(() => {});
  }, [router]);

  useEffect(() => {
    const v = videoRef.current;
    if (!v) return;
    v.play().catch(() => {
      setAnimationComplete(true);
    });
  }, []);

  const proceedToNext = () => {
    if (!animationComplete || hasNavigated) return;
    setHasNavigated(true);
    router.push('/onboarding');
  };

  return (
    <div
      onTouchStart={(e) => {
        touchStartXRef.current = e.touches[0]?.clientX ?? null;
      }}
      onTouchEnd={(e) => {
        if (!animationComplete || touchStartXRef.current === null) return;
        const endX = e.changedTouches[0]?.clientX ?? touchStartXRef.current;
        if (touchStartXRef.current - endX >= 56) {
          proceedToNext();
        }
        touchStartXRef.current = null;
      }}
      style={{
        width: '100vw',
        height: '100dvh',
        maxWidth: 480,
        margin: '0 auto',
        position: 'relative',
        overflow: 'hidden',
        background: '#fef9ef',
      }}
    >
      {/* Full-Screen Supplied 1080p Piggy-Bank & Coins Animation */}
      <video
        ref={videoRef}
        src="/piggy_bank_splash.mp4"
        autoPlay
        muted
        playsInline
        preload="auto"
        onEnded={() => setAnimationComplete(true)}
        onError={() => setAnimationComplete(true)}
        style={{
          width: '100%',
          height: '100%',
          objectFit: 'cover',
          display: 'block',
        }}
      />

      {/* Transparent touch target aligned over the video's own white circular downward arrow */}
      {animationComplete && (
        <button
          type="button"
          onClick={proceedToNext}
          aria-label="Continue to Onboarding"
          style={{
            position: 'absolute',
            bottom: '13%',
            left: '50%',
            transform: 'translateX(-50%)',
            width: 112,
            height: 112,
            borderRadius: '50%',
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            zIndex: 20,
          }}
        />
      )}
    </div>
  );
}
