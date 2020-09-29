import React from 'react'

const InputField = (props) => {
    return (
        <div>
            {props.description}: <input pattern={props.pattern} value={props.value} onChange={props.handler} />
        </div>
    )
}

export default InputField