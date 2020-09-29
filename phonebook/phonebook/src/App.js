import React, { useState, useEffect } from 'react'
import personService from './services/persons'
import Directory from './components/Directory'
import Form from './components/Form'
import Filter from './components/Filter'
import Notification from './components/Notification'
import './index.css';

const App = () => {
  const [persons, setPersons] = useState([])
  const [newName, setNewName] = useState('')
  const [newNumber, setNewNumber] = useState('')
  const [searchFilter, setSearchFilter] = useState('')
  const [notification, setNotification] = useState(null)
  const [error, setError] = useState(false)


  useEffect(() => {
    personService
      .getAll()
      .then(response => {
        setPersons(response)
      })
  }, [])

  const handleNameChange = (event) => {
    setNewName(event.target.value)
  }

  const handleNumberChange = (event) => {
    setNewNumber(event.target.value)
  }

  const handleFilterChange = (event) => {
    setSearchFilter(event.target.value)
  }

  const addPerson = (event) => {
    event.preventDefault()
    const personToAdd = { name: newName, number: newNumber }
    const oldPerson = persons.find(person => person.name === newName)
    if (!oldPerson) {

      personService
        .add(personToAdd)
        .then(addedPerson => {
          setPersons(persons.concat(addedPerson))
          setNewName('')
          setNewNumber('')
          setNotification(`Added ${addedPerson.name} to phonebook.`)
          setTimeout(() => {
            setNotification(null)
          }, 5000)
        })
        .catch(error => {
          setError(true)
          setNotification(error.response.data.error)
          setTimeout(() => {
            setNotification(null)
            setError(false)
          }, 5000)
        })

    } else {
      const result = window.confirm(`${newName} is already added to phonebook, replace the old number with the new one?`)
      if (result) {
        console.log(personToAdd)
        console.log(oldPerson)
        personToAdd.id = oldPerson.id
        personService
          .replace(personToAdd)
          .then((addedPerson) => {
            console.log(addedPerson)
            setPersons(persons.map(
              person => person.name === addedPerson.name ? addedPerson : person
            ))
            setNewName('')
            setNewNumber('')
            setNotification(`Updated ${addedPerson.name}'s number to ${addedPerson.number}.`)
            setTimeout(() => {
              setNotification(null)
            }, 5000)
          })
          .catch(error => {
            setError(true)
            if (error.response.status === 404) {
              setNotification(`${personToAdd.name} no longer exists in the phonebook`)
              setPersons(persons.filter(
                person => person.name !== personToAdd.name
              ))
            } else {
              setNotification(error.response.data.error)
            }
            setNewName('')
            setNewNumber('')
            setTimeout(() => {
              setNotification(null)
              setError(false)
            }, 5000)
          })
      }
    }
  }

  const removePerson = (person) => {
    return (event) => {
      if (window.confirm(`Delete ${person.name}?`)) {
        personService
          .remove(person)
          .then(response => {
            setPersons(
              persons.filter(element => element.id !== person.id)
            )
            setNotification(`Removed ${person.name} from the phonebook.`)
            setTimeout(() => {
              setNotification(null)
            }, 5000)
          })
      }
    }
  }

  return (

    <div>
      <h2>Phonebook</h2>

      <Filter searchFilter={searchFilter} handleFilterChange={handleFilterChange} />

      <h3>Add new entry</h3>
      
      <Notification message={notification} error={error}/>
      <Form submitHandler={addPerson} newName={newName} handleNameChange={handleNameChange} newNumber={newNumber} handleNumberChange={handleNumberChange} />
      

      <h3>Numbers</h3>

      <Directory persons={persons} searchFilter={searchFilter} remove={removePerson} />
    </div>

  )
}

export default App