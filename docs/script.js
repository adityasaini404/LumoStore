// ======================================================
// THREE.JS 3D BACKGROUND
// ======================================================
(function initThree() {
  if (typeof THREE === 'undefined') return;

  const canvas = document.getElementById('bg-canvas');
  if (!canvas) return;

  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(
    75,
    window.innerWidth / window.innerHeight,
    0.1,
    1000
  );
  camera.position.z = 5;

  const renderer = new THREE.WebGLRenderer({
    canvas,
    alpha: true,
    antialias: true
  });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

  // ---- PARTICLES ----
  const particleCount = window.innerWidth < 768 ? 800 : 2000;
  const particleGeo = new THREE.BufferGeometry();
  const positions = new Float32Array(particleCount * 3);
  const colors = new Float32Array(particleCount * 3);

  const colorA = new THREE.Color(0x6366F1);
  const colorB = new THREE.Color(0xA855F7);

  for (let i = 0; i < particleCount; i++) {
    positions[i * 3]     = (Math.random() - 0.5) * 20;
    positions[i * 3 + 1] = (Math.random() - 0.5) * 20;
    positions[i * 3 + 2] = (Math.random() - 0.5) * 20;

    const mixed = colorA.clone().lerp(colorB, Math.random());
    colors[i * 3]     = mixed.r;
    colors[i * 3 + 1] = mixed.g;
    colors[i * 3 + 2] = mixed.b;
  }

  particleGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  particleGeo.setAttribute('color', new THREE.BufferAttribute(colors, 3));

  const particleMat = new THREE.PointsMaterial({
    size: 0.03,
    vertexColors: true,
    transparent: true,
    opacity: 0.8,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  });

  const particles = new THREE.Points(particleGeo, particleMat);
  scene.add(particles);

  // ---- TORUS KNOT (wireframe) ----
  const torusGeo = new THREE.TorusKnotGeometry(1.4, 0.35, 120, 16);
  const torusMat = new THREE.MeshBasicMaterial({
    color: 0xA855F7,
    wireframe: true,
    transparent: true,
    opacity: 0.12
  });
  const torus = new THREE.Mesh(torusGeo, torusMat);
  torus.position.set(2.5, -0.5, -3);
  scene.add(torus);

  // ---- SMALL FLOATING CUBES ----
  const cubeGroup = new THREE.Group();
  const cubeMat = new THREE.MeshBasicMaterial({
    color: 0xEC4899,
    wireframe: true,
    transparent: true,
    opacity: 0.15
  });
  for (let i = 0; i < 4; i++) {
    const geo = new THREE.BoxGeometry(0.4, 0.4, 0.4);
    const cube = new THREE.Mesh(geo, cubeMat);
    cube.position.set(
      (Math.random() - 0.5) * 8,
      (Math.random() - 0.5) * 6,
      (Math.random() - 0.5) * 5 - 2
    );
    cube.userData = {
      speed: 0.3 + Math.random() * 0.5,
      axis: Math.random() > 0.5 ? 'x' : 'y'
    };
    cubeGroup.add(cube);
  }
  scene.add(cubeGroup);

  // ---- MOUSE PARALLAX ----
  let mouseX = 0, mouseY = 0;
  let targetX = 0, targetY = 0;

  window.addEventListener('mousemove', (e) => {
    mouseX = (e.clientX / window.innerWidth) * 2 - 1;
    mouseY = -(e.clientY / window.innerHeight) * 2 + 1;
  });

  // ---- SCROLL ----
  let scrollY = 0;
  window.addEventListener('scroll', () => {
    scrollY = window.scrollY;
  }, { passive: true });

  // ---- ANIMATION LOOP ----
  const clock = new THREE.Clock();

  function animate() {
    requestAnimationFrame(animate);
    const t = clock.getElapsedTime();

    targetX += (mouseX - targetX) * 0.05;
    targetY += (mouseY - targetY) * 0.05;

    particles.rotation.y = t * 0.03;
    particles.rotation.x = t * 0.015;

    torus.rotation.x = t * 0.2;
    torus.rotation.y = t * 0.3;
    torus.position.y = -0.5 + Math.sin(t * 0.5) * 0.3;

    cubeGroup.children.forEach((cube, i) => {
      const speed = cube.userData.speed;
      if (cube.userData.axis === 'x') cube.rotation.x = t * speed;
      else cube.rotation.y = t * speed;
      cube.position.y += Math.sin(t + i) * 0.002;
    });

    camera.position.x += (targetX * 0.6 - camera.position.x) * 0.05;
    camera.position.y += (targetY * 0.6 - camera.position.y + scrollY * 0.001) * 0.05;
    camera.lookAt(scene.position);

    renderer.render(scene, camera);
  }

  animate();

  window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  });
})();

// ======================================================
// HERO ICON 3D TILT (follows mouse)
// ======================================================
(function heroIconTilt() {
  const icon = document.getElementById('heroIcon');
  if (!icon) return;

  const hero = document.querySelector('.hero');
  if (!hero) return;

  hero.addEventListener('mousemove', (e) => {
    const rect = hero.getBoundingClientRect();
    const x = (e.clientX - rect.left - rect.width / 2) / rect.width;
    const y = (e.clientY - rect.top - rect.height / 2) / rect.height;

    const rotateY = x * 20;
    const rotateX = -y * 20;

    icon.style.transform = `rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-10px)`;
  });

  hero.addEventListener('mouseleave', () => {
    icon.style.transform = '';
  });
})();

// ======================================================
// 3D TILT CARDS (features)
// ======================================================
(function tiltCards() {
  document.querySelectorAll('[data-tilt]').forEach(card => {
    const inner = card.querySelector('.tilt-inner');
    if (!inner) return;

    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      const centerX = rect.width / 2;
      const centerY = rect.height / 2;

      const rotateY = ((x - centerX) / centerX) * 6;
      const rotateX = ((centerY - y) / centerY) * 6;

      card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.02)`;

      // Glare position
      inner.style.setProperty('--mx', `${(x / rect.width) * 100}%`);
      inner.style.setProperty('--my', `${(y / rect.height) * 100}%`);
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) scale(1)';
    });
  });
})();

// ======================================================
// REVEAL ON SCROLL
// ======================================================
(function revealOnScroll() {
  const elements = document.querySelectorAll('.reveal');
  if (!elements.length) return;

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });

  elements.forEach(el => observer.observe(el));
})();

// ======================================================
// MOBILE MENU
// ======================================================
(function mobileMenu() {
  const btn = document.getElementById('navMenuBtn');
  const links = document.getElementById('navLinks');
  if (!btn || !links) return;

  btn.addEventListener('click', () => {
    links.classList.toggle('open');
  });

  links.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', () => links.classList.remove('open'));
  });
})();

// ======================================================
// FOOTER YEAR
// ======================================================
(function setYear() {
  const el = document.getElementById('year');
  if (el) el.textContent = new Date().getFullYear();
})();

// ======================================================
// NAV SHADOW ON SCROLL
// ======================================================
(function navShadow() {
  const nav = document.querySelector('.nav');
  if (!nav) return;

  window.addEventListener('scroll', () => {
    if (window.scrollY > 20) {
      nav.style.boxShadow = '0 4px 24px rgba(0,0,0,0.3)';
    } else {
      nav.style.boxShadow = 'none';
    }
  }, { passive: true });
})();

console.log('%c Lumo Store ', 'background:#6366F1;color:#fff;padding:4px 8px;border-radius:4px;font-weight:bold;', '3D website loaded');