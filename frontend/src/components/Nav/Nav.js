import React from 'react';
import { NavLink } from "react-router-dom";
import * as data from './link.json';
import styles from './Nav.css';


const Links = (links) => {
    console.log(JSON.stringify(links) + "- stringify")
    return (
        <div className="menu-cards">
            {data.links.map((link) => {

                return (
                    <div key={link.href} className="card-link">
                        <NavLink to={link.href} className="card-link">
                            {link.label}
                        </NavLink>
                    </div>
                )
            })}
        </div>
    )
};

export default function Nav() {
    return (
        <div className="navi">
            <div className="logo-container">
                <span>Giga chat education</span>
            </div>

            <Links links={data}  />
        </div>
    )
}