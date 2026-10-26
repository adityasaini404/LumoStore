// ==========================================
// 1. THEME TOGGLE (Default: DARK)
// ==========================================
const themeToggleBtn = document.getElementById('theme-toggle');
const themeIcon = document.getElementById('theme-icon');
const htmlElement = document.documentElement;

// ✅ Use new key so old 'light' preference doesn't interfere
// Default is always 'dark' for fresh visitors
const savedTheme = localStorage.getItem('lumo-theme-v2') || 'dark';
htmlElement.setAttribute('data-theme', savedTheme);
themeIcon.innerText = savedTheme === 'dark' ? 'light_mode' : 'dark_mode';

themeToggleBtn.addEventListener('click', () => {
    const currentTheme = htmlElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    htmlElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('lumo-theme-v2', newTheme);
    themeIcon.innerText = newTheme === 'dark' ? 'light_mode' : 'dark_mode';
});

// ==========================================
// 2. HAMBURGER MENU (Mobile)
// ==========================================
const hamburgerBtn = document.getElementById('hamburgerBtn');
const navLinksMenu = document.getElementById('navLinks');

if (hamburgerBtn && navLinksMenu) {
    hamburgerBtn.addEventListener('click', () => {
        hamburgerBtn.classList.toggle('active');
        navLinksMenu.classList.toggle('open');
    });

    // Close menu when any link is clicked
    navLinksMenu.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', () => {
            hamburgerBtn.classList.remove('active');
            navLinksMenu.classList.remove('open');
        });
    });

    // Close menu when clicking outside
    document.addEventListener('click', (e) => {
        if (!hamburgerBtn.contains(e.target) && 
            !navLinksMenu.contains(e.target) && 
            navLinksMenu.classList.contains('open')) {
            hamburgerBtn.classList.remove('active');
            navLinksMenu.classList.remove('open');
        }
    });
}

// ==========================================
// 3. AUTO-FETCH LATEST VERSION FROM GITHUB
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    fetch('https://api.github.com/repos/adityasaini404/LumoStore/releases/latest')
        .then(response => response.json())
        .then(data => {
            if (data.tag_name) {
                document.getElementById('app-version').innerText = `${data.tag_name} · Now available`;
            }
        })
        .catch(error => {
            console.error("Error fetching version:", error);
            document.getElementById('app-version').innerText = "v1.1.0 · Now available";
        });
});

// ==========================================
// 4. THREE.JS 3D BACKGROUND
// ==========================================
const canvas = document.getElementById('webgl-canvas');
const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
const renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true });

renderer.setSize(window.innerWidth, window.innerHeight);
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

const geometry = new THREE.IcosahedronGeometry(3, 1);
const material = new THREE.MeshBasicMaterial({
    color: 0x7C3AED,
    wireframe: true,
    transparent: true,
    opacity: 0.15
});
const icosahedron = new THREE.Mesh(geometry, material);
scene.add(icosahedron);

const particlesGeometry = new THREE.BufferGeometry();
const particlesCount = 2000;
const posArray = new Float32Array(particlesCount * 3);
for (let i = 0; i < particlesCount * 3; i++) {
    posArray[i] = (Math.random() - 0.5) * 30;
}
particlesGeometry.setAttribute('position', new THREE.BufferAttribute(posArray, 3));
const particlesMaterial = new THREE.PointsMaterial({
    size: 0.03,
    color: 0x2563EB,
    transparent: true,
    opacity: 0.8,
    blending: THREE.AdditiveBlending
});
const particles = new THREE.Points(particlesGeometry, particlesMaterial);
scene.add(particles);

camera.position.z = 7;

let mouseX = 0, mouseY = 0;
document.addEventListener('mousemove', (e) => {
    mouseX = e.clientX / window.innerWidth - 0.5;
    mouseY = e.clientY / window.innerHeight - 0.5;
});

const clock = new THREE.Clock();
function animate() {
    requestAnimationFrame(animate);
    const t = clock.getElapsedTime();

    icosahedron.rotation.y = t * 0.1;
    icosahedron.rotation.x = t * 0.05;
    particles.rotation.y = t * 0.05;

    camera.position.x += (mouseX * 2 - camera.position.x) * 0.05;
    camera.position.y += (-mouseY * 2 - camera.position.y) * 0.05;
    camera.lookAt(scene.position);

    renderer.render(scene, camera);
}
animate();

window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
});

// ==========================================
// 5. CUSTOM CURSOR
// ==========================================
const cursorDot = document.querySelector('.cursor-dot');
const cursorOutline = document.querySelector('.cursor-outline');

window.addEventListener('mousemove', (e) => {
    const posX = e.clientX;
    const posY = e.clientY;
    
    cursorDot.style.left = `${posX}px`;
    cursorDot.style.top = `${posY}px`;
    
    cursorOutline.animate({
        left: `${posX}px`,
        top: `${posY}px`
    }, { duration: 500, fill: "forwards" });
});

const interactiveElements = document.querySelectorAll('.interactive');
interactiveElements.forEach(el => {
    el.addEventListener('mouseenter', () => cursorOutline.classList.add('hover'));
    el.addEventListener('mouseleave', () => cursorOutline.classList.remove('hover'));
});

// ==========================================
// 6. SCROLL REVEAL
// ==========================================
const revealElements = document.querySelectorAll('.reveal');
const revealObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('active');
            observer.unobserve(entry.target);
        }
    });
}, { threshold: 0.15 });
revealElements.forEach(el => revealObserver.observe(el));

// ==========================================
// 7. BENTO GLOW EFFECT
// ==========================================
const bentoCards = document.querySelectorAll('.bento-card');
bentoCards.forEach(card => {
    card.addEventListener('mousemove', (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        
        card.style.setProperty('--mouse-x', `${x}px`);
        card.style.setProperty('--mouse-y', `${y}px`);
    });
});

// ==========================================
// 8. DRAG TO SCROLL (Screenshots)
// ==========================================
const slider = document.querySelector('.slider-track');
if (slider) {
    let isDown = false;
    let startX;
    let scrollLeft;

    slider.addEventListener('mousedown', (e) => {
        isDown = true;
        slider.style.cursor = 'grabbing';
        startX = e.pageX - slider.offsetLeft;
        scrollLeft = slider.scrollLeft;
    });
    slider.addEventListener('mouseleave', () => {
        isDown = false;
        slider.style.cursor = 'grab';
    });
    slider.addEventListener('mouseup', () => {
        isDown = false;
        slider.style.cursor = 'grab';
    });
    slider.addEventListener('mousemove', (e) => {
        if (!isDown) return;
        e.preventDefault();
        const x = e.pageX - slider.offsetLeft;
        const walk = (x - startX) * 2;
        slider.scrollLeft = scrollLeft - walk;
    });
}

// Year
document.getElementById('year').innerText = new Date().getFullYear();