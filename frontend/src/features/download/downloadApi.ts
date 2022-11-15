import {sparqlApi} from '../sparql/sparqlApi';

// Takes the current query and transforms response to a downloadable blob. 
// Headers for Sparql endpoint are set in ./Download.tsx and ../sparql/sparqlApi.ts
export const downloadApi = sparqlApi.injectEndpoints({
  endpoints: (build) => ({
    downloadFile: build.mutation({
      query(currentQuery) {
        return {
          url: 'sparql',
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