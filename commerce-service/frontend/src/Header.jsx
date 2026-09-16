function Header() {
    return (
        <header className="site-nav">
            <div>
                <a href="/" className="site-logo">
                    The Hitchhiker's Guide to Earth
                </a>
            </div>

            <div className="site-menu">
                <span className="site-links">
                    <a href="http://localhost:8080/member/sign-in" className="site-link">
                        Sign In
                    </a>

                    <a href="http://localhost:8080/member/sign-on" className="site-link">
                        Sign On
                    </a>
                </span>
            </div>
        </header>
    );
}

export default Header;
