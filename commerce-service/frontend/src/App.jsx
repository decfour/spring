import './App.css'
import Header from './Header';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

function Home() {
    return (
        <>
          
            <div className="main-container">
                <h1 className="main-title">DON'T PANIC!</h1>
                <p className="main-sub">Every path has a story</p>

                <div className="nav-vertical">
                    <a href="http://localhost:8080/walk" className="link-item link-item-primary">
                        <span>Walk</span>
                        <span className="link-arrow">→</span>
                    </a>

                    <a href="http://localhost:8080/community" className="link-item link-item-secondary">
                        <span>Talk</span>
                        <span className="link-arrow">→</span>
                    </a>

                    <a href="http://localhost:8080/shop" className="link-item link-item-secondary">
                        <span>Shop</span>
                        <span className="link-arrow">→</span>
                    </a>

                    <a href="http://localhost:8080/notice" className="link-item link-item-secondary">
                        <span>Notice</span>
                        <span className="link-arrow">→</span>
                    </a>
                </div>
            </div>
        </>
    );
}

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App