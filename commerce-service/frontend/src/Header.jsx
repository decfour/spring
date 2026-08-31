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
                    <a href="http://localhost:8080/member/sign-in" className="global-nav-link">
                        Sign In
                    </a>

                    <a href="http://localhost:8080/member/sign-on" className="global-nav-link">
                        Sign On
                    </a>
                </span>
            </div>
        </header>
    );
}

export default Header;
