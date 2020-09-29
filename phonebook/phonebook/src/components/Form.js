import React from 'react'
import InputField from './InputField'

const Form = (props) => {
    return (
        <form onSubmit={props.submitHandler}>
          <InputField description='name' pattern='.+' value={props.newName} handler={props.handleNameChange} />
          <InputField description='number' pattern='[0-9]+(-[0-9]+)*' value={props.newNumber} handler={props.handleNumberChange} />
          <div>
            <button type="submit">add</button>
          </div>
      </form>
    )
}

export default Form