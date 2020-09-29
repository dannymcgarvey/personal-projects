require('dotenv').config()

const cors = require('cors')
const express = require('express')
const morgan = require('morgan')

const PORT = process.env.PORT
const Person = require('./models/person')

const app = express()

app.use(cors())
app.use(express.static('build'))
app.use(express.json())
app.use(morgan('tiny'))
app.use(morgan((tokens, req, res) => {
    if(tokens.method(req,res) === 'POST') {
        return JSON.stringify(req.body)
    } else {
        return ''
    }
}))


app.get('/info', (req, res) => {
    Person.find({}).then(persons => {
        res.send(
            `<div>
                <p>Phonebook has info for ${persons.length} people</p>
                <p>${new Date()}</p>
            </div>`
        )
    })
})

app.get('/api/persons', (req, res) => {
    Person.find({}).then(persons => {
        res.json(persons)
    })
    
})

app.get('/api/persons/:id', (req, res, next) => {
    Person.findById(req.params.id)
        .then(person => {
            if (person) {
                res.json(person)
            } else {
                res.status(404).end()
            } 
        })
        .catch(error => next(error))
})

app.post('/api/persons', (req, res, next) => {
    const body = req.body
    /*
    if (!body.name) {
        return res.status(400).json({
            error: 'name missing'
        })
    }

    if (!body.number) {
        return res.status(400).json({
            error: 'number missing'
        })
    }
*/
    const person = new Person({
        name: body.name,
        number: body.number
    })

    person
        .save()
        .then(savedPerson => {
            res.json(savedPerson)
        })
        .catch(error => next(error))
})

app.delete('/api/persons/:id', (req, res, next) => {

    Person.findByIdAndRemove(req.params.id)
        .then(result => {
            res.status(204).end()
        })
        .catch(error => next(error))
    
})

app.put('/api/persons/:id', (req, res, next) => {
    const body = req.body
    console.log(body)
    if (!body.name) {
        return res.status(400).json({
            error: 'name missing'
        })
    }

    if (!body.number) {
        return res.status(400).json({
            error: 'number missing'
        })
    }

    const person = {
        number: body.number
    }
    console.log('attempting to update', req.params.id)
    Person.findByIdAndUpdate(req.params.id, person, {runValidators: true, new: true})
        .then(updatedPerson => {
            res.json(updatedPerson)
        })
        .catch(error => next(error))
})

const unknownEndpoint = (req, res) => {
    res.status(404).send({ error: 'unknown endpoint' })
}
  
// handler of requests with unknown endpoint
app.use(unknownEndpoint)

const errorHandler = (error, req, res, next) => {
    console.error(error.message)
  
    if (error.name === 'CastError') {
        return res.status(400).send({ error: 'malformatted id' })
    } else if (error.name === 'ValidationError') {
        return res.status(400).send({error: error.message})
    }
  
    next(error)
}
  
app.use(errorHandler)

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`)
})