import {sparqlApi} from '../sparql/sparqlApi';

export const downloadApi = sparqlApi.injectEndpoints({
  endpoints: (build) => ({
    downloadFile: build.mutation({
      query(currentQuery) {
        return {
          url: '',
          method: "POST",
          body: {query: currentQuery},
          responseHandler: async (response) => {
            window.location.assign(
              window.URL.createObjectURL(await response.blob())
            )
          },
          cache: "no-cache",
        };
      },
    }),
  }),
  overrideExisting: false,
});

export const {
  useDownloadFileMutation,
} = downloadApi;