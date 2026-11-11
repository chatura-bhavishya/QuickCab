import { useCallback, useState } from 'react';

/** Form values keyed by input `name`, plus a shared onChange handler. */
export function useForm(initialValues) {
  const [values, setValues] = useState(initialValues);

  const handleChange = useCallback((e) => {
    const { name, value } = e.target;
    setValues(current => ({ ...current, [name]: value }));
  }, []);

  return { values, setValues, handleChange };
}
