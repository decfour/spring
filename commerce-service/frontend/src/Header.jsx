function Header() {
    return (
        <header className="global-nav">
            <div>
                <a href="/" className="global-nav-logo">
                    The Hitchhiker's Guide to Earth
                </a>
            </div>

            <div className="global-nav-menu-set">
                <span className="global-nav-link-group">
                    <a href="http://localhost:8080/member/login" className="global-nav-link">
                        Login
                    </a>

                    <a href="http://localhost:8080/member/regiseter" className="global-nav-link">
                        Register
                    </a>
                </span>
            </div>
        </header>
    );
}

export default Header;