import axios from 'axios'
const baseUrl = '/api/persons'

//return array of all persons in the server
const getAll = () => {
  const request = axios.get(baseUrl)
  return request.then(response => response.data)
}

//add a person to the server
const add = (element) => {
    const request = axios.post(baseUrl, element)
    return request.then(response => response.data)
}

const remove = (element) => {
  const request = axios.delete(`${baseUrl}/${element.id}`)
  return request.then(response => response.data)
}

const replace = (element) => {
  const request = axios.put(`${baseUrl}/${element.id}`, element)
  return request.then(response => response.data)
}

export default { getAll, add, remove, replace }