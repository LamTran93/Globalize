let hostname

if (typeof window != 'undefined') {
    hostname = window.location.hostname
}

export const NODE_URL = `http://${hostname}`
export const JAVA_URL = `http://${hostname}:8080`