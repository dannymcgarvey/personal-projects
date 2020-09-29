import React from 'react'
import Person from './Person'
const Directory = (props) => {
    return (
        props.persons.filter((person) =>
            person.name.toLowerCase().includes(
                props.searchFilter.toLowerCase())
        ).map((person) =>
            <Person key={person.number} person={person} remove={props.remove}/>)
    )
}

export default Directory