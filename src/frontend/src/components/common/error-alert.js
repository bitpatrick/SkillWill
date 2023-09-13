import React from 'react';

function ErrorAlert(props) {
    const { message } = props
    return (
        <div style={{ 
            width: '200px',
            margin: '10px',
            display: 'block',
            position: 'fixed',
            backgroundColor: 'red',
            color: 'white',
            right: '0',
            margin: '10px',
            padding: '10px',
            borderRadius: '10px',
            zIndex: '1001'
        }}>
            {message}
        </div>
    );
};

export default ErrorAlert;