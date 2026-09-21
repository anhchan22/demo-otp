import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './LandingPage.css';

// We import images as requested
import matchaClassic from '../assets/matcha-classic.png';
import strawberryMatcha from '../assets/strawberry-matcha.png';
import ubeMatcha from '../assets/ube-matcha.png';
import matchaPreparation from '../assets/matcha-preparation.png';

const useIntersectionObserver = (options = {}) => {
  const elementsRef = useRef([]);

  useEffect(() => {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('animate-fade-in-up');
          observer.unobserve(entry.target);
        }
      });
    }, options);

    elementsRef.current.forEach((el) => {
      if (el) observer.observe(el);
    });

    return () => observer.disconnect();
  }, [options]);

  return elementsRef;
};

const LandingPage = () => {
  const navigate = useNavigate();
  const animatedElements = useIntersectionObserver({ threshold: 0.1, rootMargin: '0px 0px -50px 0px' });

  const setRef = (index) => (el) => {
    if (el) animatedElements.current[index] = el;
  };

  const scrollToMenu = (e) => {
    e.preventDefault();
    document.getElementById('menu').scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className="landing-page">
      <Navbar />

      <section className="hero">
        <div className="hero-decoration hero-decoration-1"></div>
        <div className="hero-decoration hero-decoration-2"></div>
        <div className="hero-content">
          <h1 className="hero-title animate-fade-in-up" style={{ opacity: 0 }}>Little joys, everywhere you go</h1>
          <p className="hero-subtitle animate-fade-in-up delay-100" style={{ opacity: 0 }}>
            We believe the smallest details are the ones that matter most. Turn an ordinary day into something worth remembering.
          </p>
          <a href="#menu" onClick={scrollToMenu} className="hero-cta animate-fade-in-up delay-200" style={{ opacity: 0 }}>
            Khám phá menu
          </a>
        </div>
      </section>

      <section id="menu" className="products-section">
        <h2 className="section-title" ref={setRef(0)} style={{ opacity: 0 }}>Signature Drinks</h2>
        
        <div className="products-grid">
          <div className="product-card" ref={setRef(1)} style={{ opacity: 0 }}>
            <div className="product-badge badge-classic">Classic</div>
            <div className="product-image-container">
              <img src={matchaClassic} alt="Matcha Classic" className="product-image" />
            </div>
            <h3 className="product-name">Matcha Classic</h3>
            <p className="product-desc">Stone-ground ceremonial matcha over cold milk.</p>
          </div>

          <div className="product-card" ref={setRef(2)} style={{ opacity: 0 }}>
            <div className="product-badge badge-seasonal">Seasonal</div>
            <div className="product-image-container">
              <img src={strawberryMatcha} alt="Strawberry Matcha" className="product-image" />
            </div>
            <h3 className="product-name">Strawberry Matcha</h3>
            <p className="product-desc">Fresh strawberry purée layered with bright matcha.</p>
          </div>

          <div className="product-card" ref={setRef(3)} style={{ opacity: 0 }}>
            <div className="product-badge badge-new">New</div>
            <div className="product-image-container">
              <img src={ubeMatcha} alt="Ube Matcha" className="product-image" />
            </div>
            <h3 className="product-name">Ube Matcha</h3>
            <p className="product-desc">Creamy ube and matcha for a sweet, earthy swirl.</p>
          </div>
        </div>
      </section>

      <section id="about" className="about-section">
        <div className="about-content">
          <div className="about-text" ref={setRef(4)} style={{ opacity: 0 }}>
            <h2>Crafted with care</h2>
            <p>
              At Little Joys, every cup is a tribute to the timeless art of matcha. We source our leaves directly from family-owned farms in Uji, Japan, ensuring each harvest meets our uncompromising standards.
            </p>
            <p>
              Our philosophy is simple: quality ingredients, intentional preparation, and a moment of pause in your busy day.
            </p>
            <div className="about-quote">
              "The perfect balance of earthy and sweet. A true ritual."
            </div>
          </div>
          <div className="about-decoration" ref={setRef(5)} style={{ opacity: 0 }}>
            <img src={matchaPreparation} alt="Matcha preparation" className="about-image" />
          </div>
        </div>
      </section>

      <section className="cta-section">
        <div className="cta-bg-shape" style={{ width: '500px', height: '500px', top: '-250px', left: '-100px' }}></div>
        <div className="cta-bg-shape" style={{ width: '300px', height: '300px', bottom: '-100px', right: '-50px' }}></div>
        <div className="cta-content" ref={setRef(6)} style={{ opacity: 0 }}>
          <h2 className="cta-title">Trải nghiệm ngay</h2>
          <p className="cta-desc">Join the Little Joys family and turn an ordinary day into something worth remembering.</p>
          <button onClick={() => navigate('/register')} className="cta-button">
            Join the Little Joys family
          </button>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default LandingPage;
