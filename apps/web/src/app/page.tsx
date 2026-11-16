'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ChevronDown } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';

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

  // Ensure playback starts once on mount without restarting on re-renders
  useEffect(() => {
    const v = videoRef.current;
    if (!v) return;
    v.play().catch(() => {
      // Graceful fallback if browser blocks autoplay
      setAnimationComplete(true);
    });
  }, []);

  const proceedToNext = () => {
    if (!animationComplete || hasNavigated) return;
    setHasNavigated(true);
    router.push('/onboarding');
  };

  return (
    <NumoShell hideBottomNav>
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
          minHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          position: 'relative',
          padding: '24px 8px 88px',
        }}
      >
        {/* Supplied 2D Piggy-Bank & Coins Animation (Exact Asset, Uncropped) */}
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
            maxWidth: 400,
            maxHeight: '70vh',
            height: 'auto',
            objectFit: 'contain',
            borderRadius: 24,
            display: 'block',
          }}
        />

        {/* Bottom-Center Downward Arrow: Hidden Until Video Playback Ends */}
        {animationComplete && (
          <button
            type="button"
            onClick={proceedToNext}
            aria-label="Continue to Onboarding"
            className="numo-splash-arrow"
            style={{
              position: 'fixed',
              bottom: 32,
              left: '50%',
              transform: 'translateX(-50%)',
              width: 56,
              height: 56,
              borderRadius: '50%',
              border: 'none',
              background: 'linear-gradient(135deg, #14b8a6, #00796b)',
              color: '#ffffff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              boxShadow: '0 10px 24px rgba(0, 121, 107, 0.34)',
              zIndex: 40,
            }}
          >
            <ChevronDown size={30} strokeWidth={2.6} />
          </button>
        )}
      </div>
    </NumoShell>
  );
}
