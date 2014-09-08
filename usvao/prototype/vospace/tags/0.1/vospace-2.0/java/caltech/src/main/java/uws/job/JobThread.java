package uws.job;

/*
 * This file is part of UWSLibrary.
 * 
 * UWSLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UWSLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with UWSLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2011 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.util.Date;

import uws.UWSException;
import uws.UWSToolBox;

/**
 * <P>An instance of this class is a thread dedicated to a job execution.</P>
 * 
 * <P>This thread is necessary associated with an {@link AbstractJob} instance. Thus the execution of this thread ({@link JobThread#start()})
 * mainly consists in calling the {@link AbstractJob#jobWork()} method. </P>
 * 
 * <P>However its execution is possible only if the job phase is {@link ExecutionPhase#EXECUTING EXECUTING}. The job phase constraint (=EXECUTING) is
 * already checked so it is useless to check it again in {@link AbstractJob#jobWork()}.</P>
 * 
 * At the end of the execution the job must be correctly filled (cf {@link AbstractJob#jobWork()}) according to the execution conclusion:
 * <ul>
 * 	<li>SUCCESS: the <i>results</i> field of job must be filled,</li>
 * 	<li>ABORTED: the method {@link AbstractJob#abort()} must be called</li>
 * 	<li>ERROR: the <i>errors</i> field must be filled by calling {@link AbstractJob#error(ErrorSummary)} or {@link AbstractJob#error(UWSException)}.</li>
 * </ul>
 * 
 * <P>In both cases the startTime and the endTime fields are already managed by {@link AbstractJob} so it is useless to change them.</P>
 * 
 * <P>Just after the job work the job phase is set to {@link ExecutionPhase#COMPLETED COMPLETED} if no interruption has been detected, 
 * {@link ExecutionPhase#ABORTED ABORTED} otherwise.</P>
 *  
 * If {@link AbstractJob#jobWork()} throws:
 * <ul>
 * 	<li>a {@link UWSException}: the method {@link AbstractJob#error(UWSException)} is called.
 * 		Besides the exception is kept in the {@link JobThread#lastError} and is available thanks to the {@link AbstractJob#getWorkError()}.</li>
 * 	<li>an {@link InterruptedException}: the method {@link AbstractJob#abort()} is called.</li>
 * </ul>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 * 
 * @see AbstractJob#start()
 * @see AbstractJob#jobWork()
 * @see AbstractJob#abort()
 * @see AbstractJob#error(ErrorSummary)
 * @see AbstractJob#error(UWSException)
 * @see AbstractJob#getWorkError()
 */
public class JobThread extends Thread {

	/** The job which contains all parameters for its execution and which must be filled at the end of the execution. */
	protected final AbstractJob job;
	
	/** The last error which has occurred during the execution of this thread. */
	protected UWSException lastError = null;
	
	/** Indicates whether the {@link AbstractJob#jobWork()} has been called and finished, or not. */
	protected boolean finished = false;
	
	/**
	 * Builds the JobThread instance which will be used by the given job to execute its task.
	 * 
	 * @param j				The associated job.
	 * 
	 * @throws UWSException	If the given job is null.
	 */
	public JobThread(AbstractJob j) throws UWSException {
		if (j == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create a JobThread] A JobThread object can not exist without a Job instance !");
		
		job = j;
	}
	
	/**
	 * Gets the job instance associated to this thread.
	 * 
	 * @return	The associated job instance.
	 */
	public final AbstractJob getExecutedJob(){
		return job;
	}
	
	/**
	 * Gets the last error which has occurred during the execution of this thread.
	 * 
	 * @return	The last error.
	 */
	public final UWSException getError(){
		return lastError;
	}
	
	/**
	 * Indicates whether the {@link AbstractJob#jobWork()} method has been called or not.
	 * 
	 * @return	<i>true</i> if the job work is done, <i>false</i> otherwise.
	 */
	public final boolean isFinished(){
		return finished;
	}
	
	/**
	 * Lets changing the execution phase of the job and setting its endTime.
	 * 
	 * @throws UWSException	If there is an error while changing the execution phase.
	 */
	private final void complete() throws UWSException {
		if (isInterrupted())
			job.abort();
		else{
			job.setPhase(ExecutionPhase.COMPLETED);
			job.setEndTime(new Date());
		}
	}
	
	/**
	 * <ol>
	 * 	<li>Tests the execution phase of the job: if not {@link ExecutionPhase#EXECUTING EXECUTING}, nothing is done...the thread ends immediately.</li>
	 * 	<li>Calls the {@link AbstractJob#jobWork()} method.</li>
	 * 	<li>Sets the <i>finished</i> flag to <i>true</i>.</li>
	 * 	<li>Changes the job phase to {@link ExecutionPhase#COMPLETED COMPLETED} if not interrupted, else {@link ExecutionPhase#ABORTED ABORTED}.
	 * </ol>
	 * <P>If any {@link InterruptedException} occurs the job phase is only set to {@link ExecutionPhase#ABORTED ABORTED}.</P>
	 * <P>If any {@link UWSException} occurs while the phase is {@link ExecutionPhase#EXECUTING EXECUTING} the job phase
	 * is set to {@link ExecutionPhase#ERROR ERROR} and an error  summary is created.</P>
	 * <P>Whatever is the exception, it will always be available thanks to the {@link JobThread#getError()} after execution.</P>
	 * 
	 * @see AbstractJob#jobWork()
	 * @see AbstractJob#setPhase(ExecutionPhase)
	 * @see AbstractJob#publishExecutionError(UWSException)
	 * @see UWSToolBox#publishErrorSummary(AbstractJob, String, ErrorType)
	 */
	@Override
	public void run() {
		if (!job.getPhaseManager().isExecuting())
			return;
		else{
			lastError = null;
			finished = false;
		}
		
		try{
			try{
				// Execute the task:
				job.jobWork();
				
				// Change the phase to COMPLETED:
				finished = true;
				complete();
			}catch(InterruptedException ex){
				// Abort:
				finished = true;
				job.abort();
			}
			return;
			
		}catch(UWSException ue){
			// Save the error:
			lastError = ue;
			
		}catch(Throwable t){
			// Build the error:
			if (t.getMessage() == null)
				lastError = new UWSException(UWSException.INTERNAL_SERVER_ERROR, t.getClass().getName(), ErrorType.FATAL);
			else
				lastError = new UWSException(UWSException.INTERNAL_SERVER_ERROR, t, ErrorType.FATAL);
			
		}finally {
			finished = true;
			
			// Publish the error if any has occurred:
			if (lastError != null){
				try {
					job.error(lastError);
				} catch (UWSException ue) {
					lastError.printStackTrace();
					try{
						System.err.println("### [UWS ERROR - JobThread] LEVEL 1 -> Problem in "+job.getClass().getName()+".publishExecutionError(UWSException) ###");
						ue.printStackTrace();
						UWSToolBox.publishErrorSummary(job, (lastError.getCause() != null)?lastError.getCause().getMessage():lastError.getMessage(), lastError.getUWSErrorType());
					}catch(UWSException ue2){
						System.err.println("### [UWS ERROR - JobThread] LEVEL 2 -> Problem in UWSToolBox.publishErrorSummary(AbstractJob, String, ErrorType) ###");
						ue2.printStackTrace();
						try {
							job.error(new ErrorSummary(lastError.getMessage(), ErrorType.FATAL));
						} catch (UWSException ue3){
							System.err.println("### [UWS ERROR - JobThread] LEVEL 3 -> Problem in "+job.getClass().getName()+".error(ErrorSummary) or in ErrorSummary.ErrorSummary(String, ErrorType) ###");
							ue3.printStackTrace();
						}
					}
				}
			}
		}
	}
}
