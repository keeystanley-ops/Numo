'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ChevronDown } from 'lucide-react';

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

      {/* White Circular Downward Arrow with Warm Goldish Glow & Sparkles */}
      {animationComplete && (
        <div
          className="numo-splash-arrow"
          style={{
            position: 'absolute',
            bottom: 42,
            left: '50%',
            transform: 'translateX(-50%)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            zIndex: 30,
          }}
        >
          <svg width="36" height="14" viewBox="0 0 36 14" fill="none" aria-hidden="true">
            <path d="M18 2V12" stroke="#F59E0B" strokeWidth="2.5" strokeLinecap="round" />
            <path d="M8 5L11 12" stroke="#F59E0B" strokeWidth="2.2" strokeLinecap="round" />
            <path d="M28 5L25 12" stroke="#F59E0B" strokeWidth="2.2" strokeLinecap="round" />
          </svg>
          <button
            type="button"
            onClick={proceedToNext}
            aria-label="Continue to Onboarding"
            style={{
              width: 62,
              height: 62,
              borderRadius: '50%',
              border: '2px solid rgba(251, 191, 36, 0.55)',
              background: '#ffffff',
              color: '#134e4a',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              boxShadow: '0 0 26px rgba(245, 158, 11, 0.55), 0 8px 20px rgba(45, 31, 20, 0.14)',
            }}
          >
            <ChevronDown size={30} strokeWidth={2.7} />
          </button>
        </div>
      )}
    </div>
  );
}
